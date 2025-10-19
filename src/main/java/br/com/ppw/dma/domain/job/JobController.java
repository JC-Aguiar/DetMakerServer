package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.cliente.ClienteService;
import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.pipeline.PipelineService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Slf4j
@RestController
@RequestMapping("job")
public class JobController extends MasterController<Long, Job, JobController> {

    private ModelMapper mapper;
    private ModelMapper mapperFlex;
    private JobService jobService;
    private ClienteService clienteService;
    private PipelineService pipelineService;


    @Autowired
    public JobController(
        @Qualifier("Basic") ModelMapper mapper,
        @Qualifier("Flex") ModelMapper mapperFlex,
        JobService jobService,
        ClienteService clienteService,
        PipelineService pipelineService)
    {
        super(jobService);
        this.mapper = mapper;
        this.mapperFlex = mapperFlex;
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

    @PutMapping
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

    @Transactional
    @PostMapping("cliente/{clienteId}")
    public ResponseEntity<JobInfoDTO> saveNew(
        @PathVariable("clienteId") Long clienteId,
        @Validated @RequestBody JobInfoDTO dto)
    {
        var cliente = clienteService.findById(clienteId);

        log.info("Criando Job {} para Cliente {} com base nos campos enviados.", dto.getNome(), cliente.getNome());
        var job = mapper.map(dto, Job.class);
        job.setCliente(cliente);
        finishJobFieldsFromDto(dto, job);

        job = jobService.save(job);
        dto.setId(job.getId());

        return ResponseEntity.ok(dto);
    }

    @Transactional
    @PostMapping("cliente/{clienteId}/all")
    public ResponseEntity<?> saveNew(
        @PathVariable() Long clienteId,
        @Validated @RequestBody List<JobInfoDTO> jobsDto)
    {
        if(jobsDto.isEmpty()) {
            return ResponseEntity.badRequest().body("A lista de Jobs enviada está vazia.");
        }
        val cliente = clienteService.findById(clienteId);
        log.info("Salvando Jobs no banco.");
        int jobsSalvosComSucesso = 0;
        int jobsComErro = 0;

        log.info("Criando {} Job(s) para Cliente {} com base nos campos enviados.", jobsDto.size(), cliente.getNome());
        for(val dto: jobsDto) {
            try {
                var job = mapper.map(dto, Job.class);
                job.setCliente(cliente);
                finishJobFieldsFromDto(dto, job);

                job = jobService.save(job);
                dto.setId(job.getId());
                jobsSalvosComSucesso++;
            }
            catch(Exception e) {
                log.warn("Erro ao tentar salvar Job '{}': {}.", dto.getNome(), e.getMessage());
                jobsComErro++;
            }
        }
        log.info("Total de Jobs salvos: {}.", jobsSalvosComSucesso);
        if(jobsComErro > 0)
            log.warn("Total de Jobs com erro: {}.", jobsComErro);

        return ResponseEntity.ok(jobsDto);
    }

    @Transactional
    @PutMapping("cliente/{clienteId}")
    public ResponseEntity<?> update(
        @PathVariable("clienteId") Long clienteId,
        @Validated @RequestBody JobInfoDTO dto)
    {
        if(dto.getId() == null) {
            return ResponseEntity.badRequest().body("Não é possível atualizar um Job sem informar seu ID.");
        }
        //var cliente = clienteService.findById(clienteId);
        var job = jobService.findById(dto.getId());

        log.info("Atualizando Job {} [ID {}] com base nos campos enviados.", job.getNome(), job.getId());
        var originalId = job.getId();
        mapper.map(dto, job);
        job.setId(originalId);
        finishJobFieldsFromDto(dto, job);

        jobService.save(job);
        return ResponseEntity.ok(dto);
    }

    private void finishJobFieldsFromDto(JobInfoDTO dto, Job job) {
        job.setDataAtualizacao(OffsetDateTime.now());
        job.setAtualizadoPor("DET-MAKER"); //TODO: mudar para nome do usuário logado da requisição
        setJobParametersFromDto(job, dto.getParametros());
        setJobCargasFromDto(job, dto.getMascaraEntrada());
        setJobLogsFromDto(job, dto.getMascaraLog());
        setJobRemessasFromDto(job, dto.getMascaraSaida());
    }

    private Job setJobParametersFromDto(@NonNull Job job, @NonNull List<JobParamDTO> parametrosDto) {
        var jobParam = parametrosDto.stream()
            .map(JobParamDTO::converterParaEntidade)
            .toList();
        job.setParametros(jobParam);
        return job;
    }

    private Job setJobCargasFromDto(@NonNull Job job, @NonNull  List<JobResourceDTO> cargasDto) {
        var jobCarga = cargasDto.stream()
            .map(JobResourceDTO::converterParaEntidade)
            .toList();
        job.setCargas(jobCarga);
        return job;
    }

    private Job setJobLogsFromDto(@NonNull Job job, @NonNull List<JobResourceDTO> logsDto) {
        var jobLog = logsDto.stream()
            .map(JobResourceDTO::converterParaEntidade)
            .toList();
        job.setLogs(jobLog);
        return job;
    }

    private Job setJobRemessasFromDto(@NonNull Job job, @NonNull List<JobResourceDTO> remessasDto) {
        var jobRemessa = remessasDto.stream()
            .map(JobResourceDTO::converterParaEntidade)
            .toList();
        job.setRemessas(jobRemessa);
        return job;
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
    //TODO: precisa criar novo mapeamento para Parâmetros, Cargas, Logs e Remessas
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
            .map(dto -> mapper.map(dto, Job.class))
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

}
