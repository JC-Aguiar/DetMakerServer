package br.com.ppw.dma.job;

import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.movie.MovieDtoRequest;
import br.com.ppw.dma.movie.MovieDtoResponse;
import br.com.ppw.dma.movie.MovieEntity;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;
import static br.com.ppw.dma.util.FormatString.dividirValores;

@RestController
@RequestMapping("job")
@Slf4j
public class AgendaController extends MasterController
    <AgendaID, Agenda, MovieDtoRequest, AgendaDTO, AgendaController> {

    private final AgendaService agendaService;
    public static final String PLANILHA_NOME = "DIÁRIA";

    public AgendaController(@Autowired AgendaService agendaService) {
        super(agendaService);
        this.agendaService = agendaService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping(value = "/")
    public ResponseEntity<?> getAgenda(
        @RequestParam("arquivo") String arquivo,
        @RequestParam("planilha") String planilha,
        @RequestParam("id") Long id) {
        //--------------------------------------
        val agenda = agendaService.findById(new AgendaID(id, planilha, arquivo));
        return ResponseEntity.ok(agenda);
    }

    //TODO: javadoc
    @PostMapping(value = "open/xlsx")
    @Transactional
    public ResponseEntity<?> abrirXlsx(@RequestParam("file") final MultipartFile file) throws IOException {
        val xlsx = agendaService.lerXlsx(file);
        val agendasDto = agendaService.mapearPlanilhaParaListaDto(xlsx, PLANILHA_NOME);
        log.info("Total de agendas obtidas: {}", agendasDto.size());

        log.info("Salvando agendas no banco local H2.");
        int agendasSalvas = 0;
        for(val dto: agendasDto) {
            log.info("Convertendo DTO em Entidade.");
            val agendaEntidade = getModelMapper().map(dto, Agenda.class);
            agendaEntidade.setId(new AgendaID(dto.getId(), dto.getNomePlanilha(), dto.getNomeArquivo()));
            agendaService.persist(agendaEntidade);
            agendasSalvas++;
        }
        log.info("Total de agendas salvas: {}", agendasSalvas);
        return ResponseEntity.ok(agendasDto);
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
            .map(agendaService::executarPilha)
            .toList();
        log.info("Total de jobs executadas: {}.", pilha.size());

        val sucessos = pilha.stream()
            .filter(Evidencia::isSucesso)
            .toList()
            .size();
        log.info("Total de jobs realizados com sucesso: {}.", sucessos);

        log.info("Encerrando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        if(sucessos > 0) return ResponseEntity.ok(pilha);
        return ResponseEntity
            .internalServerError()
            .body("Todas os jobs falharam. Mais detalhes no log, consulte o ID " + serviceId);
    }

    private Evidencia converterItemPilhaDtoEmEntidade(@NonNull ItemPilhaDTO postDTO) {
        final Evidencia evidencia = getModelMapper().map(postDTO, Evidencia.class);
        evidencia.setRegistro(postDTO.getAgenda());
        return evidencia;
    }

    //TODO: javadoc
    private ItemPilhaDTO setAgendaDto(ItemPilhaDTO postDTO) {
        try {
            log.info("Buscando registro do arquivo '{}', planilha '{}', job-id {}.",
                postDTO.getId().getNomeArquivo(),
                postDTO.getId().getNomePlanilha(),
                postDTO.getId().getId()
            );
            val agenda = agendaService.findById(postDTO.getId());
            log.info("Entidade Agenda encontrada: {}.", agenda);
            log.info("Mascara Log ('agenda'): {}", agenda.getMascaraLog());

            val agendaDto = converterAgendaEmDto(agenda);
            postDTO.setAgenda(agendaDto);
            return postDTO;
        }
        catch(Exception e) {
            return null;
        }
    }

    private AgendaDTO converterAgendaEmDto(@NonNull Agenda agenda) {
        log.info("Convertendo Agenda para AgendaDTO.");
        val agendaDto = getModelMapper().map(agenda, AgendaDTO.class);
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
