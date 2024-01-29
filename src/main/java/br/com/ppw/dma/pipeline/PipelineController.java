package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.cliente.ClienteService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.job.JobController;
import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.relatorio.RelatorioService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("pipeline")
public class PipelineController extends MasterController<Long, Pipeline, PipelineController> {

    private final PipelineService pipelineService;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AmbienteService ambienteService;

    @Autowired
    private JobController jobController;

    @Autowired
    private RelatorioService relatorioService;


    public PipelineController(@Autowired PipelineService pipelineService) {
        super(pipelineService);
        this.pipelineService = pipelineService;
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
     * @param execDTO {@link PipelineExecDTO} contendo as informações necessárias para execução.
     * @return {@link ResponseEntity} com o {@link RelatorioHistoricoDTO} do Relatório final.
     */
    @PostMapping(value = "run")
    public ResponseEntity<RelatorioHistoricoDTO> runNewPipeline(
        @NonNull @RequestBody PipelineExecDTO execDTO) {
        //-------------------------------------------------------
        //Coleta ou cria Ambiente
        val pipeline = getAndUpdate(execDTO.getPipeline())
            .orElseGet(() -> proxy().createNewPipeline(execDTO));

        //Executa os Jobs e obtêm as evidências
        val ambiente = ambienteService.findById(execDTO.getAmbienteId());
        val banco = AmbienteAcessoDTO.banco(ambiente);
        val ftp = AmbienteAcessoDTO.ftp(ambiente);
        val evidencias = jobController.executeJobsAndGetEvidencias(banco, ftp, execDTO.getJobs());

        //Coleta todos os parâmetros de entrada dos Jobs
        val parametrosDosJobs = evidencias.stream()
            .map(ev -> ev.getJob().getNome() +" "+ ev.getArgumentos())
            .collect(Collectors.joining("\n"));

        //Cria e salva novo relatório
        val relatorio = relatorioService.buildAndPersist(
            ambiente,
            execDTO.getRelatorio(),
            pipeline,
            evidencias,
            parametrosDosJobs
        );
        log.info("Montando RelatorioHistoricoDTO.");
        val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
        log.info(relatorioHistorico.toString());

        return ResponseEntity.ok(relatorioHistorico);
    }

    @PostMapping(value = "new")
    public ResponseEntity<?> nova(@Valid @RequestBody PipelineInfoDTO dto)
    throws DuplicatedRecordException {
        log.debug("Iniciando validação contra duplicidade, conversão e persistência.");
        pipelineService.checkDuplicated(dto.getNome(), dto.getClienteId());
        val cliente = clienteService.findById(dto.getClienteId());
        val jobs = jobController.findByClienteAndNome(cliente, dto.getJobs());
        val pipeline = Pipeline.parseInfoDto(dto, jobs, cliente);
        pipelineService.persist(pipeline);

        log.info("Resposta ao cliente:");
        dto.setJobs( jobs.stream().map(Job::getNome).toList() );
        log.info(dto.toString());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "update")
    public ResponseEntity<String> atualizar(@NonNull @RequestBody PipelineInfoDTO pipeline) {
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

        val jobService = jobController.getService();
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
        val cliente = pipelineBanco.getProps().getCliente();
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
                pipelineBanco.setJobs(jobController.findByClienteAndNome(cliente, dto.getJobs()));
            pipelineService.persist(pipelineBanco);
        }
        return pipeline;
    }

}
