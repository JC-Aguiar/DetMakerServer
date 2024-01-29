package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.Job;
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
public class PipelineInfoDTO {

    @NotNull Long id;
    @NotNull String nome;
    @NotNull String descricao;
    @NotNull Long clienteId;
    @NotNull List<String> jobs = new ArrayList<>();

    public PipelineInfoDTO(@NonNull Pipeline pipeline) {
        this.id = pipeline.getId();
//        this.nome = pipeline.getProps().getNome();
        this.nome = pipeline.getNome();
        this.descricao = pipeline.getDescricao();
//        this.clienteId = pipeline.getProps().getCliente().getId();
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
