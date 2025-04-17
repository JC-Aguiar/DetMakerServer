package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.cliente.ClienteService;
import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.pipeline.PipelineService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static br.com.ppw.dma.util.FormatString.valorVazio;

@Slf4j
@RestController
@RequestMapping("job")
public class JobController extends MasterController<Long, Job, JobController> {

    private ModelMapper mapper;
    private JobService jobService;
    private ClienteService clienteService;
    private PipelineService pipelineService;


    @Autowired
    public JobController(
        ModelMapper mapper,
        JobService jobService,
        ClienteService clienteService,
        PipelineService pipelineService)
    {
        super(jobService);
        this.mapper = mapper;
        this.jobService = jobService;
        this.clienteService =  clienteService;
        this.pipelineService = pipelineService;
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<?> getAll(@PathVariable("clienteId") Long clienteId) {
        final List<JobInfoDTO> dtos = jobService.findAllByCliente(clienteId)
            .stream()
            .map(JobInfoDTO::converterJob)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("cliente/{clienteId}")
    public ResponseEntity<JobInfoDTO> save(
        @PathVariable("clienteId") Long clienteId,
        @RequestBody JobInfoDTO dto)
    {
        dto.setDiretorioEntrada(valorVazio(dto.getDiretorioEntrada()));
        dto.setDiretorioSaida(valorVazio(dto.getDiretorioSaida()));
        dto.setDiretorioLog(valorVazio(dto.getDiretorioLog()));

        var cliente = clienteService.findById(clienteId);
        var job = Optional.ofNullable(dto.getId())
            .map(id -> {
                var entidade = jobService.findById(id);
                mapper.map(dto, entidade);
                entidade.setParametros(String.join(", ", dto.getParametros()));
                entidade.setDescricaoParametros(String.join(", ", dto.getDescricaoParametros()));
                entidade.setMascaraEntrada(String.join(", ", dto.getMascaraEntrada()));
                entidade.setMascaraLog(String.join(", ", dto.getMascaraLog()));
                entidade.setMascaraSaida(String.join(", ", dto.getMascaraSaida()));
                return entidade;
            })
            .orElseGet(() -> mapper.map(dto, Job.class));
        job.setCliente(cliente);
        job.setDataAtualizacao(OffsetDateTime.now());
        job.setAtualizadoPor("DET-MAKER"); //TODO: mudar para nome do usuário
        job.refinarCampos();
        jobService.save(job);

        dto.setId(job.getId());
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<?> parseOne(Job entity) {
        final JobInfoDTO dto = JobInfoDTO.converterJob(entity);
        return ResponseEntity.ok(dto);
    }

    /**
     * Método herdado do {@link MasterController} para conversão de Entidade para DTO
     * @param jobs {@link Page} de {@link Job}s a converter
     * @return {@link ResponseEntity} contendo uma {@link Page} de {@link JobInfoDTO}s
     */
    @Override
    public ResponseEntity<Page<JobInfoDTO>> parseAll(Page<Job> jobs) {
        final Page<JobInfoDTO> responsePage = jobs.map(JobInfoDTO::converterJob);
        return ResponseEntity.ok(responsePage);
    }

    //TODO: javadoc
    //TODO: criar novo controlar para ter essa responsabilidade?
    @Transactional
    @PostMapping(value = "read/xlsx/planilha/{planilhaNome}/cliente/{clienteId}/usuario/{userEmail}")
    public ResponseEntity<String> readXlsx(
        @PathVariable() String planilhaNome,
        @PathVariable() Long clienteId,
        @PathVariable() String userEmail,
        @RequestParam("file") final MultipartFile file)
    throws IOException {
        val cliente = clienteService.findById(clienteId);
        log.info("Lendo arquivo Excel (formato xlsx) para Cliente '{}'.", cliente.getNome());
        val excelX = jobService.lerXlsx(file);
        val jobsDto = jobService.mapExcelToJobInfoDto(excelX, planilhaNome);
        log.info("Total de jobs mapeados da planilha: {}.", jobsDto.size());

        log.info("Convertendo DTOs em Entidades.");
        var dataHoraHoje = OffsetDateTime.now(RELOGIO);
        var entidades = jobsDto.stream()
            .peek(dto -> log.debug(dto.toString()))
            .map(dto -> mapper.map(dto, Job.class).refinarCampos())
            .peek(job -> job.setCliente(cliente))
            .peek(job -> job.setDataAtualizacao(dataHoraHoje))
            .peek(job -> job.setAtualizadoPor(userEmail))
            .toList();

        log.info("Persistindo Jobs no banco.");
        var totalSalvos = jobService.save(entidades)
            .stream()
            .filter(job -> job.getId() != -1)
            .count();

        var mensagem = "Total de Jobs salvos com sucesso = " + totalSalvos;
        log.info(mensagem + ".");
        return ResponseEntity.ok(mensagem);
    }

    //TODO: javadoc
    @Transactional
    @PostMapping(value = "save/all/cliente/{clienteId}")
    public ResponseEntity<?> saveJobs(
        @PathVariable() Long clienteId,
        @NonNull @RequestBody List<JobInfoDTO> jobsDto) {
        //----------------------------------------------------
        if(jobsDto.isEmpty()) {
            return ResponseEntity.badRequest().body("A lista de Jobs enviada está vazia.");
        }
        val cliente = clienteService.findById(clienteId);
        log.info("Salvando jobs no banco.");
        int jobsSalvos = 0;
        for(val dto: jobsDto) {
            try {
                log.debug(dto.toString());
                log.info("Convertendo DTO em Entidade.");
                val jobEntidade = mapper.map(dto, Job.class).refinarCampos();
                jobService.persist(jobEntidade);
                jobsSalvos++;
            }
            catch(Exception e) {
                log.warn("Erro ao tentar salvar Job ID {}: {}.", dto.getNome(), e.getMessage());
            }
        }
        val mensagem = "Total de jobs salvos: " +jobsSalvos+ ".";
        log.info(mensagem);

        if(jobsSalvos == 0) {
            return ResponseEntity
                .internalServerError()
                .body("Não foi possível salvar nenhum dos jobs enviados.");
        }
        return ResponseEntity.ok(mensagem);
    }


    @PatchMapping
    public ResponseEntity<JobInfoDTO> getAll(@Validated @RequestBody JobInfoDTO dto) {
        log.info("Consultando no banco Job [ID {}].", dto.getId());
        var job = Optional.ofNullable(dto.getId())
            .map(jobService::findById)
            .orElseThrow(() -> new NoSuchElementException("ID " +dto.getId()+ " não encontrado."));

        log.info("Job encontrado:");
        log.info(job.toString());

        log.info("Atualizando novos campos.");
        mapper.map(dto, job);
        log.info(job.toString());

        log.info("Salvando Job atualizado.");
        jobService.save(job);

        return ResponseEntity.ok(dto);
    }

    @Override
    @Transactional
    @DeleteMapping("id/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") Long...id)
    throws NoSuchMethodException {
        log.info("Identificando Jobs dos IDs: {}", Arrays.deepToString(id));
        var jobs = jobService.findById(Set.of(id));
        var idsBanco = jobs.stream()
            .peek(job -> log.info(job.toString()))
            .map(Job::getId)
            .collect(Collectors.toSet());

        log.info("Removendo Jobs solicitados das Pipelines que fazem parte.");
        var pipelines = jobs.stream()
            .map(Job::getPipelines)
            .flatMap(Collection::stream)
            .peek(pipe -> log.info("Pipeline '{}' [ID {}] impactada.", pipe.getNome(), pipe.getId()))
            .toList();
        pipelines.forEach(pipe -> pipe.getJobs().removeAll(jobs));
        pipelineService.save(pipelines);

        log.info("Deletando Jobs.");
        jobService.delete(jobs);
        return ResponseEntity.ok(
            "Jobs ID deletados: " +idsBanco+ ".\n Total: " + idsBanco.size() + "."
        );
    }

//    @Override
//    @DeleteMapping("id/{id}")
//    public ResponseEntity<String> delete(@PathVariable(name = "id") Long id)
//    throws NoSuchMethodException {
//        log.info("Identificando Job ID {}.", id);
//        var job = jobService.findById(id);
//        log.info(job.toString());
//
//        log.info("Removendo Job '{}' [ID {}] das Pipelines relacionadas.", job.getNome(), job.getId());
//        var pipelines = job.getPipelines();
//        pipelines.forEach(pipe -> pipe.getJobs().remove(job));
//        pipelineService.save(pipelines);
//
//        log.info("Deletando Job '{}' [ID {}].", job.getNome(), job.getId());
//        jobService.delete(job);
//        return ResponseEntity.ok(
//            "Job ID " +id+ " deletado com sucesso. " +
//            "Total de Pipelines impactadas: " +pipelines.size()
//        );
//    }

}
