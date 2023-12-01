package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.user.UserInfoDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineNovaExecDTO extends PipelineNovaDTO {

    UserInfoDTO userInfo;
    List<JobExecuteDTO> jobs;

}
