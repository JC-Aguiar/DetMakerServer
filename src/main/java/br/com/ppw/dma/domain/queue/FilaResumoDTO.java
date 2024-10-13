package br.com.ppw.dma.domain.queue;

import lombok.Builder;
import lombok.NonNull;

import java.time.OffsetDateTime;

@Builder
public record FilaResumoDTO(

    long id,
    String ticket,
    String nomePipeline,
    String autor,
    QueueStatus status,
    OffsetDateTime data) {


    public static FilaResumoDTO converterEntidade(@NonNull TaskQueue queue) {
        return FilaResumoDTO.builder()
            .id(queue.getId())
            .ticket(queue.getTicket())
            .nomePipeline(queue.getPipeline())
            .autor(queue.getUsuario())
            .data(queue.getDataSolicitacao())
            .status(queue.getStatus())
            .build();
    }

}
