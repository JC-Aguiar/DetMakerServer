package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.Job;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Valid
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineInfoDTO {

    @NotNull @Min(0) Long id;
    @NotBlank @Size(max = 200) String nome;
    @NotNull @Size(max = 500) String descricao;
    @NotNull @Min(0) Long clienteId;
    List<String> jobs = new ArrayList<>();


    public PipelineInfoDTO(@NonNull Pipeline pipeline) {
        this.id = pipeline.getId();
        this.nome = pipeline.getNome();
        this.descricao = pipeline.getDescricao();
        this.clienteId = pipeline.getCliente().getId();
        setJobs(pipeline.getJobs());
    }

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
