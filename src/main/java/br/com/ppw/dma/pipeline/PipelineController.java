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
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
    public ResponseEntity<?> getAll(@PathVariable(name = "clienteId") Long clienteId) {
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
    @PostMapping(value = "run") //"run/pipelineId/{pipelineId}/ambienteId/{ambienteId}")
    public ResponseEntity<RelatorioHistoricoDTO> setAndRun(
//        @PathVariable Long pipelineId,
//        @PathVariable Long ambienteId,
        @Valid @RequestBody PipelineExecDTO execDto) {
        //----------------------------------------------------------
        var pipeline = pipelineService.findById(execDto.getPipelineId()); //pipelineId);
        var ambiente = ambienteService.findById(execDto.getAmbienteId()); //ambienteId);
        var jobs = Set.copyOf(execDto.getJobs())
            .stream()
            .map(dto -> jobService.findById(dto.getId()))
            .toList();

        log.debug("Preparando execução dos Jobs, convertendo os JobExecuteDTOs para JobExecutePOJOs.");
        var jobsPreparation = execDto.getJobs()
            .stream()
            .map(dto -> {
                var job = jobs.stream()
                    .filter(j -> dto.getId().equals(j.getId()))
                    .findFirst()
                    .orElse(null);
                return new JobPreparation(JobInfoDTO.converterJob(job), dto);
            })
            .toList();

        return run(
            new PipelinePreparation(
                pipeline,
                execDto.getAtividade(),
                ambiente,
                jobsPreparation
        ));
    }

    @Synchronized
    public ResponseEntity<RelatorioHistoricoDTO> run(@NonNull PipelinePreparation preparation) {
        val jobsProcessados = jobService.prepararExecutar(preparation);
        val evidencias = evidenciaService.gerarEvidencia(jobsProcessados);
        val relatorio = relatorioService.buildAndPersist(preparation, evidencias);
        val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
        return ResponseEntity.ok(relatorioHistorico);
    }

    @PostMapping(value = "new")
    public ResponseEntity<?> createNew(@Valid @RequestBody PipelineInfoDTO dto)
    throws DuplicatedRecordException {
        pipelineService.checkDuplicated(dto.getNome(), dto.getClienteId());
        val cliente = clienteService.findById(dto.getClienteId());
        val jobs = jobService.findByClienteAndNome(cliente, dto.getJobs());
        val pipeline = Pipeline.parseInfoDto(dto, jobs, cliente);
        pipelineService.persist(pipeline);

        dto.setJobs(
            jobs.stream().map(Job::getNome).toList()
        );
        log.info("Resposta ao cliente:");
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

//    public Pipeline createNewPipeline(@NonNull PipelineExecDTO execDTO) {
//        log.info("Criando nova Pipeline '{}' para Cliente ID {}.",
//            execDTO.getPipelineId().getNome(), execDTO.getClienteId());
//        val cliente = clienteService.findById(execDTO.getClienteId());
//
//        log.info("Obtendo todos os Jobs listados no DTO.");
//        val jobIds = execDTO.getJobs()
//            .stream()
//            .map(JobExecuteDTO::getId)
//            .toList();
//        final List<Job> jobs = jobService.findAllById(jobIds);
//        val jobsNome = jobs.stream()
//            .map(Job::getNome)
//            .collect(Collectors.joining(", "));
//        log.info("Total de {} Jobs encontrados:", jobs.size());
//        log.info(" - {}", jobsNome);
//
//        val pipeline = Pipeline.parseInfoDto(execDTO.getPipelineId(), jobs, cliente);
//        return pipelineService.persist(pipeline);
//    }

    public Optional<Pipeline> getAndUpdate(@NonNull PipelineInfoDTO dto) {
        val pipeline = pipelineService.getUniqueOne(dto.getNome(), dto.getClienteId());
        if(pipeline.isEmpty()) return pipeline;

        val pipelineBanco = pipeline.get();
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

    @DeleteMapping(value = "clientId/{clientId}/pipeline/{nome}")
    public ResponseEntity<String> delete(
        @PathVariable(name = "clientId") Long clientId,
        @PathVariable(name = "nome") String nome) {
        //----------------------------------------
        var pipeline = pipelineService.getUniqueOne(nome, clientId);
        if (pipeline.isPresent()) {
            pipeline.get().setOcultar(true);
            pipelineService.persist(pipeline.get());
        }
        else {
            return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body("Pipeline '" + nome + "' não encontrada.");
        }
        return ResponseEntity.ok("Pipeline '" + nome + "' deletada com sucesso.");
    }

}
