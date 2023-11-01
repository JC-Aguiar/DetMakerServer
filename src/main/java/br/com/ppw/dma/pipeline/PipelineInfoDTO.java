package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineInfoDTO implements MasterRequestDTO, MasterResponseDTO {

    @NotNull String nome;
    @NotNull String descricao;
    List<String> jobs = new ArrayList<>();

    public void setJobs(List jobs) {
        if(jobs == null || jobs.isEmpty()) return;
        if(jobs.get(0) instanceof String) {
            this.jobs = jobs;
        }
        else if(jobs.get(0) instanceof Job) {
            this.jobs = ((List<Job>) jobs).stream()
                .map(Job::getNome)
                .toList();
        }
    }
}
