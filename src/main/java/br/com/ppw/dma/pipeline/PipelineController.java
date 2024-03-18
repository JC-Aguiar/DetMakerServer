package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.cliente.ClienteService;
import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.job.*;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.relatorio.RelatorioService;
import br.com.ppw.dma.system.FileSystemService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("pipeline")
public class PipelineController extends MasterController<Long, Pipeline, PipelineController> {

    @Autowired
    private ModelMapper mapper;

    private PipelineService pipelineService;

    private ClienteService clienteService;

    private AmbienteService ambienteService;

    private JobService jobService;

    private RelatorioService relatorioService;

    private EvidenciaService evidenciaService;

    private FileSystemService fileSystemService;


    public PipelineController(
        @Autowired PipelineService pipelineService,
        @Autowired ClienteService clienteService,
        @Autowired AmbienteService ambienteService,
        @Autowired JobService jobService,
        @Autowired RelatorioService relatorioService,
        @Autowired EvidenciaService evidenciaService,
        @Autowired FileSystemService fileSystemService) {
        //--------------------------------------------
        super(pipelineService);
        this.pipelineService = pipelineService;
        this.clienteService = clienteService;
        this.ambienteService = ambienteService;
        this.jobService = jobService;
        this.relatorioService = relatorioService;
        this.fileSystemService = fileSystemService;
        this.evidenciaService = evidenciaService;
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<?> getAll( @PathVariable(name = "clienteId") Long clienteId) {
        final List<PipelineInfoDTO> dtos = pipelineService.findAllByCliente(clienteId)
            .stream()
            .map(PipelineInfoDTO::new)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<?> parseOne(Pipeline entity) {
        final PipelineInfoDTO dto = new PipelineInfoDTO(entity);
        return ResponseEntity.ok(dto);
    }

    /**
     * Método herdado do {@link MasterController} para conversão de Entidade para DTO
     * @param pipelines {@link Page} de {@link Pipeline}s a converter
     * @return {@link ResponseEntity} contendo uma {@link Page} de {@link PipelineInfoDTO}s
     */
    @Override
    public ResponseEntity<Page<PipelineInfoDTO>> parseAll(Page<Pipeline> pipelines) {
        final Page<PipelineInfoDTO> responsePage = pipelines.map(PipelineInfoDTO::new);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * Principal funcionalidade de toda a aplicação. Em ordem:<ol>
 *     <li>Identificação do Ambiente</li>
     * <li>Obtenção dos acessos ao Banco e FTP do Ambiente</li>
     * <li>Execução da pilha de Jobs</li>
     * <li>Coleta das Evidências de cada Job</li>
     * <li>Detalhamento do processo no Relatório</li>
     * </ol>
     * @param execDto {@link PipelineExecDTO} contendo as informações necessárias para execução.
     * @return {@link ResponseEntity} com o {@link RelatorioHistoricoDTO} do Relatório final.
     */
    //TODO: aplicar @Synchronized e testar.
    //  Se funcionar, o timeout do front precisa ser atualizado com base na quantidade de espera na fila
    @Transactional
    @PostMapping(value = "run")
    public ResponseEntity<RelatorioHistoricoDTO> updateAndRun(@Valid @RequestBody PipelineExecDTO execDto) {
        val pipeline = getAndUpdate(execDto.getPipeline())
            .orElseGet(() -> proxy().createNewPipeline(execDto));
        val ambiente = ambienteService.findById(execDto.getAmbienteId());
//        val banco = AmbienteAcessoDTO.banco(ambiente);
//        val ftp = AmbienteAcessoDTO.ftp(ambiente);

        //TODO: O looping abaixo deve ser um for manual.
        //  Validar se o Job ID atual já não foi processado.
        //  Evitar consultas desnecessárias no banco.
        log.debug("Preparando execução dos Jobs, convertendo os JobExecuteDTOs para JobExecutePOJOs.");
        final List<JobPreparation> jobs = execDto.getJobs()
            .stream()
            .map(dto -> {
                val id = dto.getId();
                log.info("Buscando registro para Job id {}.", id);
                val job = jobService.findById(id);
                log.info(job.toString());
//                val cargas = new ArrayList<File>();
//                if(!dto.getCargas().isEmpty()) {
//                    log.info("Total de arquivos de carga: {}.", dto.getCargas());
//                    log.info("Salvando as cargas dos Jobs no diretório temporário.");
//                    dto.getCargas()
//                        .stream()
//                        .map(fileSystemService::store)
//                        .forEach(cargas::add);
//                }
//                else log.info("Nenhum arquivo de carga foi informado na requisição.");
//                return new JobProcess(job, dto, banco, cargas);
                return new JobPreparation(JobInfoDTO.converterJob(job), dto);
            })
            .toList();

        return run(
            new PipelinePreparation(pipeline, execDto.getRelatorio(), ambiente, jobs));
    }


    public ResponseEntity<RelatorioHistoricoDTO> run(@NonNull PipelinePreparation preparation) {
//        val evidencias = jobService.executarJob(ftp, jobsPojo);
        val jobsProcessados = jobService.executarJob(preparation);
        val evidencias = evidenciaService.gerarEvidencia(jobsProcessados);
        val relatorio = relatorioService.buildAndPersist(
            preparation.relatorio(),
            preparation.ambiente(),
            preparation.pipeline(),
            evidencias);

        log.info("Montando RelatorioHistoricoDTO.");
        val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
        log.info(relatorioHistorico.toString());
        return ResponseEntity.ok(relatorioHistorico);
    }

//    @Scope("dev")
//    @PostMapping(value = "teste")
//    public ResponseEntity<String> teste(@ModelAttribute JobExecuteDTO execDto) {
//        val tempDir = System.getProperty("java.io.tmpdir");
//        log.info("Diretório temporário: " + tempDir);
//        log.info(execDto.toString());
//        log.info("Gerando arquivos temporários:");
//        execDto.getCargas().forEach(carga -> {
//            log.info("- Nome: '{}' ({})", carga.getOriginalFilename(), carga.getSize());
//            val temp = Arquivos.parseMultipartFile(carga);
//            if(temp.isPresent()) {
//                log.info("- Diretório Usado: {}", temp.get().getAbsolutePath());
//                log.info("- Nome Gerado: {}", temp.get().getName());
//                log.info("- Conteúdo Lido:");
//                System.out.println(Arquivos.lerArquivo(temp.get()));
//            }
//            else
//                log.warn("Não foi possível criar arquivo temporário");
//        });
//        return ResponseEntity.ok("Ok");
//    }

    @PostMapping(value = "new")
    public ResponseEntity<?> createNew(@Valid @RequestBody PipelineInfoDTO dto)
    throws DuplicatedRecordException {
        log.debug("Iniciando validação contra duplicidade, conversão e persistência.");
        pipelineService.checkDuplicated(dto.getNome(), dto.getClienteId());
        val cliente = clienteService.findById(dto.getClienteId());
        val jobs = jobService.findByClienteAndNome(cliente, dto.getJobs());
        val pipeline = Pipeline.parseInfoDto(dto, jobs, cliente);
        pipelineService.persist(pipeline);

        log.info("Resposta ao cliente:");
        dto.setJobs( jobs.stream().map(Job::getNome).toList() );
        log.info(dto.toString());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "update")
    public ResponseEntity<String> update(@NonNull @RequestBody PipelineInfoDTO pipeline) {
        if(getAndUpdate(pipeline).isPresent())
            return ResponseEntity.ok("Pipeline atualizada");

        val mensagem = String.format("Pipeline %s não encontrada no banco para Cliente ID %d",
            pipeline.getNome(), pipeline.getClienteId());
        throw new NoSuchElementException(mensagem);
    }

//    public JobExecuteDTO evidenciaParaJobExecuteDto(@NonNull Evidencia evidencia) {
//        val id = evidencia.getProps();
//        val comandosSql = evidencia.getBanco()
//            .stream()
//            .map(q -> new ComandoSql(q.getTabelaNome()))
//            .toList();
//        val comandoSqlString = comandosSql.stream()
//            .map(ComandoSql::getTabela)
//            .collect(Collectors.joining(", "));
//        log.info("Tablas usadas pela Evidência ID {}: {}.", id, comandoSqlString);
//
//        val cargas = evidencia.getCargas()
//            .stream()
//            .map(ExecFile::getArquivo)
//            .toList();
//        log.info("Cargas usadas na Evidência ID {}:", id);
//        cargas.forEach(c -> log.info(" - {}", c));
//
//        return JobExecuteDTO.builder()
//            .id(evidencia.getJob().getProps())
//            .ordem(evidencia.getOrdem())
//            .argumentos(evidencia.getArgumentos())
//            .queries(comandosSql)
//            .cargas(cargas)
//            .build();
//    }

    public Pipeline createNewPipeline(@NonNull PipelineExecDTO execDTO) {
        log.info("Criando nova Pipeline '{}' para Cliente ID {}.",
            execDTO.getPipeline().getNome(), execDTO.getClienteId());

        val cliente = clienteService.findById(execDTO.getClienteId());

        log.info("Obtendo todos os Jobs listados no DTO.");
        val jobIds = execDTO.getJobs()
            .stream()
            .map(JobExecuteDTO::getId)
            .toList();
        final List<Job> jobs = jobService.findAllById(jobIds);
        val jobsNome = jobs.stream()
            .map(Job::getNome)
            .collect(Collectors.joining(", "));
        log.info("Total de {} Jobs encontrados:", jobs.size());
        log.info(" - {}", jobsNome);

        val pipeline = Pipeline.parseInfoDto(execDTO.getPipeline(), jobs, cliente);
        return pipelineService.persist(pipeline);
    }

    public Optional<Pipeline> getAndUpdate(@NonNull PipelineInfoDTO dto) {
        val pipeline = pipelineService.getUniqueOne(dto.getNome(), dto.getClienteId());
        if(pipeline.isEmpty()) return pipeline;

        val pipelineBanco = pipeline.get();
//        val cliente = pipelineBanco.getProps().getCliente();
        val cliente = pipelineBanco.getCliente();
        log.info("Comparando Pipelines.");
        log.info("Pipelines Usuário: {}", dto);
        log.info("Pipelines Banco: {}", pipelineBanco);
        val atualizarDescricao = pipelineBanco.atualizarDescricao(dto.getDescricao());
        val atualizarJobs = pipelineBanco.atualizarJobs(dto.getJobs());

        if(atualizarDescricao || atualizarJobs) {
            log.info("A Ambiente precisa ser atualizada.");
            if(atualizarDescricao)
                pipelineBanco.setDescricao(dto.getDescricao());
            if(atualizarJobs)
                pipelineBanco.setJobs(jobService.findByClienteAndNome(cliente, dto.getJobs()));
            pipelineService.persist(pipelineBanco);
        }
        return pipeline;
    }

}
