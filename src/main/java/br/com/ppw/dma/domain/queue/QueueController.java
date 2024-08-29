package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.master.MasterController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("queue")
public class QueueController extends MasterController<Long, Queue, QueueController> {

    private final QueueService queueService;


    @Autowired
    public QueueController(QueueService queueService) {
        super(queueService);
        this.queueService = queueService;
    }

    @Override
    public ResponseEntity<?> parseOne(Queue entity) {
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<?> parseAll(Page<Queue> entities) {
        return ResponseEntity.ok(entities);
    }

}
