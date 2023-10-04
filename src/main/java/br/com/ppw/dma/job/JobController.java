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
    @Transactional
    public ResponseEntity<?> abrirXlsx(@RequestParam("file") final MultipartFile file) throws IOException {
        val xlsx = jobService.lerXlsx(file);
        val jobsDto = jobService.mapearPlanilhaParaListaDto(xlsx, PLANILHA_NOME);
        log.info("Total de agendas obtidas: {}", jobsDto.size());

        log.info("Salvando agendas no banco local H2.");
        int jobsSalvos = 0;
        for(val dto: jobsDto) {
            log.info("Convertendo DTO em Entidade.");
            val jobEntidade = getModelMapper().map(dto, Job.class);
            //jobEntidade.setId(new JobID(dto.getId(), dto.getJob()));
            jobService.persist(jobEntidade);
            jobsSalvos++;
        }
        log.info("Total de agendas salvas: {}", jobsSalvos);
        return ResponseEntity.ok(jobsDto);
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
            .map(this::setAgendaDto)
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
        evidenciaPOJO.setRegistro(postDTO.getAgenda());
        return evidenciaPOJO;
    }

    //TODO: javadoc
    private ItemPilhaDTO setAgendaDto(ItemPilhaDTO postDTO) {
        try {
            log.info("Buscando registro do job id {}.", postDTO.getId());
            val job = jobService.findById(postDTO.getId());
            log.info("Entidade Job encontrada: {}.", job);
            log.info("Mascara Log ('agenda'): {}", job.getMascaraLog());

            val agendaDto = converterJobEmDto(job);
            postDTO.setAgenda(agendaDto);
            return postDTO;
        }
        catch(Exception e) {
            return null;
        }
    }

    //TODO: javadoc
    private JobDTO converterJobEmDto(@NonNull Job agenda) {
        log.info("Convertendo Job para JobDTO.");
        val agendaDto = getModelMapper().map(agenda, JobDTO.class);
        agendaDto.setParametros(dividirValores(agenda.getParametros()));
        agendaDto.setDescricaoParametros(dividirValores(agenda.getDescricaoParametros()));
        agendaDto.setMascaraEntrada(dividirValores(agenda.getMascaraEntrada()));
        agendaDto.setMascaraSaida(dividirValores(agenda.getMascaraSaida()));
        agendaDto.setMascaraLog(dividirValores(agenda.getMascaraLog()));

        log.info("Conversão realizada com sucesso.");
        log.info("{}", agendaDto);
        return agendaDto;
    }


}
