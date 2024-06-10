package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.relatorio.AtividadeInfoDTO;
import br.com.ppw.dma.user.UserInfoDTO;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@ToString()
@NoArgsConstructor
@EqualsAndHashCode()
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineExecDTO {

    //TODO: posteriormente o `UserInfoDTO user` deverá ser substituído pelos dados no JWT

    @NotNull Long clienteId;
    @NotNull Long ambienteId;
    @NotNull Long pipelineId;
    @NotNull AtividadeInfoDTO atividade;
    @NotNull UserInfoDTO user;
    @NotNull List<JobExecuteDTO> jobs;

}
