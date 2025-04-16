package br.com.ppw.dma.domain.task;

import lombok.Builder;
import lombok.NonNull;

import java.time.OffsetDateTime;

@Builder
public record TaskInfoDTO(

//    long id,
    String ticket,
    String nomePipeline,
    String autor,
    TaskStatus status,
    OffsetDateTime data) {


    public static TaskInfoDTO converterEntidade(@NonNull RemoteTask queue) {
        return TaskInfoDTO.builder()
//            .id(queue.getId())
            .ticket(queue.getTicket())
            .nomePipeline(queue.getPipelineNome())
            .autor(queue.getUsuario())
            .data(queue.getDataSolicitacao())
            .status(queue.getStatus())
            .build();
    }

}
