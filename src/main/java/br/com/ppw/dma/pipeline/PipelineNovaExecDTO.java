package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.user.UserInfoDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineNovaExecDTO extends PipelineNovaDTO {

    UserInfoDTO userInfo;
    List<JobExecuteDTO> jobs;

}
