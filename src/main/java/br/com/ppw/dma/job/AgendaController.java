package br.com.ppw.dma.job;

import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.movie.MovieDtoRequest;
import br.com.ppw.dma.movie.MovieDtoResponse;
import br.com.ppw.dma.movie.MovieEntity;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;

@RestController
@RequestMapping("job")
@Slf4j
public class AgendaController extends MasterController
    <Integer, MovieEntity, MovieDtoRequest, MovieDtoResponse, AgendaController> {

    private final ModelMapper modelMapper;
    private final AgendaService agendaService;
    public static final String PLANILHA_NOME = "DIÁRIA";

    public AgendaController(@Autowired AgendaService agendaService, @Autowired ModelMapper modelMapper) {
        super(agendaService);
        this.agendaService = agendaService;
        this.modelMapper = modelMapper;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    //TODO: javadoc
    @PostMapping(value = "open/xlsx")
    public ResponseEntity<?> abrirXlsx(@RequestParam("file") final MultipartFile file) throws IOException {
        val xlsx = agendaService.lerXlsx(file);
        val agendasDto = agendaService.mapearPlanilhaParaListaDto(xlsx, PLANILHA_NOME);
        log.info("Total de agendas obtidas: {}", agendasDto.size());

        val agendasSalvas = agendasDto
            .stream()
            .map((element) -> modelMapper.map(element, Agenda.class))
            .map(agendaService::persist)
            .count();
        log.info("Total de agendas salvas no banco: {}", agendasSalvas);
        return ResponseEntity.ok(agendasDto);
    }

    //TODO: javadoc
    @PostMapping(value = "execute/stack")
    public ResponseEntity<?> executarPilha(@RequestParam("stack") final List<ItemPilhaPostDTO> pilha) {
        val serviceId = "$" + Instant.now().toEpochMilli();
        log.info("Iniciando Serviço {} {} {}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);

        val novaPilha = pilha.stream()
            .map(this::setAgendaDto)
            .map(this::converterItemPilhaDtoEmEntidade)
            .map(agendaService::executarPilha)
            .toList();
        log.info("Total de jobs executadas: {}.", novaPilha.size());

        val sucessos = novaPilha.stream()
            .filter(ItemPilha::isSucesso)
            .toList()
            .size();
        log.info("Total de jobs realizados com sucesso: {}.", sucessos);

        log.info("Finalizado Serviço {} {} {}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        if(sucessos > 0) return ResponseEntity.ok(novaPilha);
        return ResponseEntity
            .internalServerError()
            .body("Todas os jobs falharam. Mais detalhes no log, consulte o ID " + serviceId);
    }

    private ItemPilha converterItemPilhaDtoEmEntidade(ItemPilhaPostDTO postDTO) {
        final ItemPilha<AgendaDTO> itemPilha = modelMapper.map(postDTO, ItemPilha.class);
        itemPilha.setRegistro(postDTO.getAgenda());
        return itemPilha;
    }

    //TODO: javadoc
    private ItemPilhaPostDTO setAgendaDto(ItemPilhaPostDTO postDTO) {
        try {
            log.info("Buscando registro do arquivo '{}', planilha '{}', job-id {}.",
                postDTO.getId().getNomeArquivo(),
                postDTO.getId().getNomePlanilha(),
                postDTO.getId().getId()
            );
            val agenda = agendaService.findById(postDTO.getId());
            log.info("Entidade Agenda encontrada: {}.", agenda);

            log.info("Convertendo para AgendaDTO.");
            val agendaDto = modelMapper.map(agenda, AgendaDTO.class);
            log.info("Conversão realizada com sucesso.");
            log.info("{}", agendaDto);

            postDTO.setAgenda(agendaDto);
            return postDTO;
        }
        catch(NoSuchMethodException e) {
            return null;
        }
    }


}
