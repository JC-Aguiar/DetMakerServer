package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.relatorio.RelatorioInfoDTO;
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

    @NotNull Long clienteId;
    @NotNull Long ambienteId;
    @NotNull PipelineInfoDTO pipeline;
    @NotNull RelatorioInfoDTO relatorio;
    @NotNull UserInfoDTO userInfo;
    @NotNull List<JobExecuteDTO> jobs;

}
