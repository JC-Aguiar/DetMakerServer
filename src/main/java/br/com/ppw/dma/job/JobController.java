package br.com.ppw.dma.job;

import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.cliente.ClienteService;
import br.com.ppw.dma.configQuery.ConfigQueryController;
import br.com.ppw.dma.evidencia.EvidenciaController;
import br.com.ppw.dma.master.MasterController;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@RestController
@RequestMapping("job")
@Slf4j
public class JobController extends MasterController<Long, Job, JobController> {

    @Autowired
    private ModelMapper mapper;

    private JobService jobService;

    private EvidenciaController evidenciaController;

    private ConfigQueryController queryController;

    private ClienteService clienteService;

    private AmbienteService ambienteService;


    public JobController(
        @Autowired JobService jobService,
        @Autowired EvidenciaController evidenciaController,
        @Autowired ConfigQueryController queryController,
        @Autowired ClienteService clienteService,
        @Autowired AmbienteService ambienteService) {
        //-------------------------------------
        super(jobService);
        this.jobService = jobService;
        this.evidenciaController = evidenciaController;
        this.queryController = queryController;
        this.clienteService =  clienteService;
        this.ambienteService = ambienteService;
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<?> getAll( @PathVariable(name = "clienteId") Long clienteId) {
        final List<JobInfoDTO> dtos = jobService.findAllByCliente(clienteId)
            .stream()
            .map(JobInfoDTO::converterJob)
            .toList();
        return ResponseEntity.ok(dtos);
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
        var entidades = jobsDto.stream()
            .peek(dto -> log.debug(dto.toString()))
            .map(dto -> mapper.map(dto, Job.class).refinarCampos())
            .peek(job -> job.setCliente(cliente))
            .peek(job -> job.setDataAtualizacao(OffsetDateTime.now(RELOGIO)))
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

}
