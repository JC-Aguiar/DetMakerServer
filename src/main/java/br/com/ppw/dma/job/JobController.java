package br.com.ppw.dma.job;

import br.com.ppw.dma.evidencia.EvidenciaPOJO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterDtoRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;
import static br.com.ppw.dma.util.FormatString.dividirValores;

@RestController
@RequestMapping("job")
@Slf4j
public class JobController extends MasterController
    <Long, Job, MasterDtoRequest, JobDTO, JobController> {

    private final JobService jobService;
    public static final String PLANILHA_NOME = "DIÁRIA";

    public JobController(@Autowired JobService jobService) {
        super(jobService);
        this.jobService = jobService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    //TODO: remover ou sobrescrever da classe-mãe
    @GetMapping(value = "/")
    public ResponseEntity<?> getJob(
        @RequestParam(value = "id", required = false) Long id,
        @RequestParam(value = "nome", required = false) String nome) {
        //--------------------------------------
        Job job = null;
        if(id != null)
            job = jobService.findById(id);
        else if(nome != null && !nome.trim().isEmpty())
            job = jobService.findByNome(nome);
        else
            return ResponseEntity.badRequest().body("Informe o ID ou o NOME do job");
        return ResponseEntity.ok(job);
    }

    //TODO: javadoc
    @PostMapping(value = "open/xlsx")
    public ResponseEntity<?> abrirXlsx(@RequestParam("file") final MultipartFile file) throws IOException {
        val xlsx = jobService.lerXlsx(file);
        val jobsDto = jobService.mapearPlanilhaParaListaDto(xlsx, PLANILHA_NOME);
        log.info("Total de jobs mapeados da planilha: {}.", jobsDto.size());
        return ResponseEntity.ok(jobsDto);
    }

    //TODO: javadoc
    @Transactional
    @PostMapping(value = "save/all")
    public ResponseEntity<?> salvarJobs(@RequestBody List<JobDTO> jobsDto) {
        log.info("Salvando jobs no banco.");
        if(jobsDto.isEmpty()) {
            return ResponseEntity.badRequest()
                .body("A lista de jobs enviada está vazia.");
        }
        int jobsSalvos = 0;
        for(val dto: jobsDto) {
            log.info("{}.", dto);
            log.info("Convertendo DTO em Entidade.");
            val jobEntidade = getModelMapper().map(dto, Job.class);
            try {
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
    @PostMapping(value = "execute/stack")
    public ResponseEntity<?> executarPilha(@RequestBody List<ItemPilhaDTO> pilhaDto) {
        //TODO: mover para AspectJ, para tornar padrão em todos os serviços
        val serviceId = "$" + Instant.now().toEpochMilli();
        log.info("Iniciando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);

        //TODO: separar a Evidência do Resumo, onde o Resumo contêm as informações da execução
        //  (aonde teve sucesso e aonde teve falha)

        val pilha = pilhaDto.stream()
            .map(this::setjobDto)
            .map(this::converterItemPilhaDtoEmEntidade)
            .map(jobService::executarPilha)
            .toList();
        log.info("Total de jobs executadas: {}.", pilha.size());

        val sucessos = pilha.stream()
            .filter(EvidenciaPOJO::isSucesso)
            .toList()
            .size();
        log.info("Total de jobs realizados com sucesso: {}.", sucessos);

        log.info("Encerrando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        if(sucessos > 0) return ResponseEntity.ok(pilha);
        return ResponseEntity
            .internalServerError()
            .body("Todas os jobs falharam. Mais detalhes no log, consulte o ID " + serviceId);
    }

    //TODO: javadoc
    private EvidenciaPOJO converterItemPilhaDtoEmEntidade(@NonNull ItemPilhaDTO postDTO) {
        final EvidenciaPOJO evidenciaPOJO = getModelMapper().map(postDTO, EvidenciaPOJO.class);
        evidenciaPOJO.setRegistro(postDTO.getJob());
        return evidenciaPOJO;
    }

    //TODO: javadoc
    private ItemPilhaDTO setjobDto(ItemPilhaDTO postDTO) {
        try {
            log.info("Buscando registro do job id {}.", postDTO.getId());
            val job = jobService.findById(postDTO.getId());
            log.info("Entidade Job encontrada: {}.", job);
            log.info("Mascara Log ('job'): {}", job.getMascaraLog());

            val jobDto = converterJobEmDto(job);
            postDTO.setJob(jobDto);
            return postDTO;
        }
        catch(Exception e) {
            return null;
        }
    }

    //TODO: javadoc
    private JobDTO converterJobEmDto(@NonNull Job job) {
        log.info("Convertendo Job para JobDTO.");
        val jobDto = getModelMapper().map(job, JobDTO.class);
        jobDto.setParametros(dividirValores(job.getParametros()));
        jobDto.setDescricaoParametros(dividirValores(job.getDescricaoParametros()));
        jobDto.setMascaraEntrada(dividirValores(job.getMascaraEntrada()));
        jobDto.setMascaraSaida(dividirValores(job.getMascaraSaida()));
        jobDto.setMascaraLog(dividirValores(job.getMascaraLog()));

        log.info("Conversão realizada com sucesso.");
        log.info("{}", jobDto);
        return jobDto;
    }


}
