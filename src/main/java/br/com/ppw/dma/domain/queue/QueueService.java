package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.pipeline.Pipeline;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Slf4j
@Service
@EnableAsync
public class QueueService extends MasterService<Long, Queue, QueueService> {

    private ApplicationEventPublisher publisher;
    private ObjectMapper objectMapper;
    private final QueueRepository queueDao;

    private record ExecQueueEvent(@NonNull Queue queue) {}
    private record AwaitQueueEvent(@NonNull Queue queue) {}


    @Autowired
    public QueueService(
        ApplicationEventPublisher publisher,
        ObjectMapper objectMapper,
        QueueRepository queueDao) {

        super(queueDao);
        this.queueDao = queueDao;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Queue persist(@NotNull Queue queue) {
        log.info("Persistindo Queue no banco:");
        log.info(queue.toString());
        queue = queueDao.save(queue);

        log.info("Evidência ID {} gravado com sucesso.", queue.getId());
        return queue;
    }

    public Long countByStatusInAmbiente(
        @NonNull Ambiente ambiente,
        @NonNull QueueStatus status) {

        return queueDao.countByStatusInAmbiente(ambiente, status.name());
    }

    public Long countInAmbiente(@NonNull Ambiente ambiente) {
        return queueDao.countInAmbiente(ambiente);
    }

    public QueuePushResponseDTO pushQueueItem(
        @NonNull Ambiente ambiente,
        @NonNull Pipeline pipeline,
        @NonNull String usuario,
        @NonNull QueuePayload payload)
    throws JsonProcessingException {
//        var itensExecutando = countByStatusInAmbiente(ambiente, EXECUTANDO);
//        var queueSize = countByStatusInAmbiente(ambiente, AGUARDANDO);
        var queueSize = countInAmbiente(ambiente);
        if(queueSize > 0) {
            return QueuePushResponseDTO.blocked(queueSize);
        }
        var ticket = UUID.randomUUID().toString();
        log.info("Ticket desta solicitação: {}.", ticket);

        var json = objectMapper.writeValueAsString(payload);
        var itemFila = Queue.builder()
            .ticket(ticket)
            .ambiente(ambiente)
            .pipeline(pipeline.getNome())
            .usuario(usuario)
            .payload(json)
            .dataSolicitacao(OffsetDateTime.now(RELOGIO))
            .build();
        publisher.publishEvent(itemFila);

        return new QueuePushResponseDTO(ticket, queueSize);
    }


    @Async
    @EventListener
    public void pushQueueEvent(@NonNull Queue queue) {
        /*
        TODO:
         1. Validar quantidade de registros
         */

    }

}
