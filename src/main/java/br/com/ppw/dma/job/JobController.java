package br.com.ppw.dma.job;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.evidencia.EvidenciaController;
import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterRequestDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static br.com.ppw.dma.util.FormatString.dividirValores;

@RestController
@RequestMapping("job")
@Slf4j
public class JobController extends MasterController
    <Long, Job, MasterRequestDTO, JobInfoDTO, JobController> {

    private final JobService jobService;
    private final EvidenciaController evidenciaController;
    public static final String PLANILHA_NOME = "DIÁRIA";

    public JobController(
        @Autowired JobService jobService,
        @Autowired EvidenciaController evidenciaController) {
        //----------------------------------------------------
        super(jobService);
        this.jobService = jobService;
        this.evidenciaController = evidenciaController;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Override
    public ResponseEntity<Page<JobInfoDTO>> getAll(int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final Page<Job> entitiesPage = jobService.findAll(pageConfig);
        final Page<JobInfoDTO> responsePage = entitiesPage.map(this::converterJobEmDto);
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    //TODO: javadoc
    //TODO: criar novo controlar para ter essa responsabilidade
    @PostMapping(value = "open/xlsx")
    public ResponseEntity<List<JobInfoDTO>> openXlsx(
        @RequestParam("file") final MultipartFile file)
    throws IOException {
        log.info("Acionando leitura de arquivo Excel XLSX.");
        val xlsx = jobService.lerXlsx(file);
        val jobsDto = jobService.mapExcelToJobDto(xlsx, PLANILHA_NOME);

        log.info("Total de jobs mapeados da planilha: {}.", jobsDto.size());
        return ResponseEntity.ok(jobsDto);
    }

    //TODO: javadoc
    @Transactional
    @PostMapping(value = "save/all")
    public ResponseEntity<?> saveJobs(@RequestBody List<JobInfoDTO> jobsDto) {
        log.info("Salvando jobs no banco.");
        if(jobsDto.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("A lista de jobs enviada está vazia   .");
        }
        int jobsSalvos = 0;
        for(val dto: jobsDto) {
            try {
                log.info("Convertendo DTO em Entidade.");
                log.info(dto.toString());
                val jobEntidade = getModelMapper()
                    .map(dto, Job.class)
                    .refinarCampos();

                jobService.persist(jobEntidade);
                jobsSalvos++;
            }
            catch(Exception e) {
                log.warn("Erro ao tentar salvar job [{}] {}: {}.",
                    dto.getId(), dto.getNome(), e.getMessage());
            }
        }
        val mensagem = "Total de jobs salvos: " +jobsSalvos+ ".";
        log.info(mensagem);

        if(jobsSalvos == 0) {
            return ResponseEntity.internalServerError()
                .body("Não foi possível salvar nenhum dos jobs enviados.");
        }
        return ResponseEntity.ok(mensagem);
    }

    //TODO: javadoc
    //TODO: mover para AspectJ, para tornar padrão em todos os serviços
    @PostMapping(value = "execute/stack")
    public ResponseEntity<?> executeJobs(@RequestBody List<JobExecuteDTO> jobsExecute) {
        val evidenciasDto = executeJobsAndGetEvidencias(jobsExecute)
            .stream()
            .map(env -> evidenciaController.parseToResponseDto(env, env.getOrdem()))
            .toList();
        val sucesso = evidenciasDto.stream().anyMatch(EvidenciaInfoDTO::getSucesso);
        if(sucesso) return ResponseEntity.ok(evidenciasDto);
        return ResponseEntity.internalServerError().body(evidenciasDto);
    }

    public List<Evidencia> executeJobsAndGetEvidencias(List<JobExecuteDTO> jobsExecute) {
        //TODO: separar a Evidência do Resumo, onde o Resumo contêm as informações da execução
        //  (aonde teve sucesso e aonde teve falha)
        //Será convertido o JobExecuteDTO para JobExecutePOJO, inserindo nele a entidade Job via ID.
        //Também será gerado JobInfoDTO com base na entidade. DTO responsável pela execução dos comandos
        //de forma correta e organizada no terminal SFTP
        log.info("Iniciando rotina da execução de Jobs");
        val jobsPojo = jobsExecute.stream()
            .map(this::createPojo)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(jobService::executarPilha)
            .toList();
        log.info("Total de Jobs executados: {}.", jobsPojo.size());

        val sucessos = jobsPojo.stream()
            .filter(JobExecutePOJO::isSucesso)
            .toList()
            .size();
        log.info("Total de Jobs com sucesso: {}.", sucessos);

        //TODO: comentar etapas abaixo
        return evidenciaController.gerarEvidencias(jobsPojo);
    }

    //TODO: javadoc
    private Optional<JobExecutePOJO> createPojo(@NonNull JobExecuteDTO dto) {
        try {
            log.info("Buscando registro para Job id {}.", dto.getId());
            val job = jobService.findById(dto.getId());
            log.info("Job encontrado:");
            log.info(job.toString());

            log.info("Criando objeto JobInfoDTO de execução.");
            val jobInfo = converterJobEmDto(job);
            log.info(jobInfo.toString());

            log.info("Agrupando objetos dentro do JobExecutePOJO.");
            val jobPojo = getModelMapper().map(dto, JobExecutePOJO.class);
            jobPojo.setJob(job);
            jobPojo.setJobInfo(jobInfo);
            jobPojo.addComandoSql(dto.getQueries());
            log.info(jobPojo.toString());

            return Optional.of(jobPojo);
        }
        catch(Exception e) {
            log.warn("Erro durante preparo do Job id {}: {}.", dto.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    //TODO: javadoc
    private JobInfoDTO converterJobEmDto(@NonNull Job job) {
        log.info("Convertendo Job para JobInfoDTO.");
        val jobDto = getModelMapper().map(job, JobInfoDTO.class);
        jobDto.setParametros(dividirValores(job.getParametros()));
        jobDto.setTabelas(dividirValores(job.getTabelas()));
        jobDto.setDescricaoParametros(dividirValores(job.getDescricaoParametros()));
        jobDto.setMascaraEntrada(dividirValores(job.getMascaraEntrada()));
        jobDto.setMascaraSaida(dividirValores(job.getMascaraSaida()));
        jobDto.setMascaraLog(dividirValores(job.getMascaraLog()));

        log.info("Conversão realizada com sucesso.");
        log.info("{}", jobDto);
        return jobDto;
    }

    @DeleteMapping(value = "all")
    public ResponseEntity<String> deleteAll() {
        try {
            jobService.deleteAll();
            return ResponseEntity.ok("Todos os jobs foram deletados com sucesso.");
        }
        catch(Exception e) {
            log.warn(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
