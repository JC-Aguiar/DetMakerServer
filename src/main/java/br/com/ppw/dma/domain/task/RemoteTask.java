package br.com.ppw.dma.domain.task;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class RemoteTask {

    /**
     * Identificador da solicitação de um acionamento
     */
    @Builder.Default
    final String ticket = UUID.randomUUID().toString();

    /**
     * Ambiente em que serão executadas os comando no Payload
     */
    @Positive
    final long ambienteId;

    /**
     * Nome da Pipeline
     */
    @NotBlank
    @Size(max = 200)
    final String pipelineNome;

    /**
     * Nome do usuário
     */
    @NotBlank
    @Size(max = 200)
    final String usuario;

    /**
     * Conteúdo Json da Pipeline solicitada
     */
    @ToString.Exclude
    @NotBlank
    final String payload;

    /**
     * Data e hora em que a Pipeline foi solicitada
     */
    @NotNull
    OffsetDateTime dataSolicitacao;

    /**
     * Data e hora em que a Pipeline foi executada
     */
    @Nullable
    OffsetDateTime dataExecucao;

    /**
     * Status dessa solicitação
     */
    @NotNull
    TaskStatus status;

}
