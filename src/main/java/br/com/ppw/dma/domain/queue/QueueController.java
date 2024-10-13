package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.master.MasterController;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RestController
@RequestMapping("queue")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class QueueController extends MasterController<Long, TaskQueue, QueueController> {

    QueueService queueService;
    AmbienteService ambienteService;


    @Autowired
    public QueueController(QueueService queueService, AmbienteService ambienteService) {
        super(queueService);
        this.queueService = queueService;
        this.ambienteService = ambienteService;
    }

    @Override
    public ResponseEntity<?> parseOne(TaskQueue entity) {
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<?> parseAll(Page<TaskQueue> entities) {
        return ResponseEntity.ok(entities);
    }

    //TODO: criar um DTO para receber essa requisição
    @GetMapping("summary/ambiente/{ambienteId}")
    public ResponseEntity<Page<FilaResumoDTO>> getAllSummarized(
        @PathVariable(name = "ambienteId") Long ambienteId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens,
        @RequestParam(name = "ticket") Optional<String> ticket,
        @RequestParam(name = "nomePipeline") Optional<String> nomePipeline,
        @RequestParam(name = "autor") Optional<String> autor) {

        var pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        var ambiente = ambienteService.findById(ambienteId);
        var relatorioBusca = TaskQueue.builder()
            .ticket(ticket.orElse(null))
            .pipeline(nomePipeline.orElse(null))
            .usuario(autor.orElse(null))
            .ambiente(ambiente)
            .build();
        Example<TaskQueue> exemplo = Example.of(relatorioBusca, MATCHER_ALL);
        Page<FilaResumoDTO> relatorios = queueService.findAllByExample(exemplo, pageConfig)
            .map(FilaResumoDTO::converterEntidade);
        return ResponseEntity.ok(relatorios);
    }

}
