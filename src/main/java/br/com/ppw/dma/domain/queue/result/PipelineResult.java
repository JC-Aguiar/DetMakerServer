package br.com.ppw.dma.domain.queue.result;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineResult {

    @NonNull
    final String ticket;

    @NonNull
    final String usuario;

    @NonNull
    final String pipelineNome;

    @NonNull
    final String pipelineDescricao;

    @NonNull
    final String clienteNome;

    @NonNull
    @Builder.Default
    String mensagemErro = "";

    @Builder.Default
    final List<JobResult> resultadoJobs = new ArrayList<>();

    @Builder.Default
    final List<EvidenciaResult> resultadoEvidencias = new ArrayList<>();

    @Builder.Default
    boolean erro = false;


    public List<JobResult> getResultadoJobs() {
        return List.copyOf(resultadoJobs);
    }

    public List<EvidenciaResult> getResultadoEvidencias() {
        return List.copyOf(resultadoEvidencias);
    }

    public void addJobResult(@NonNull JobResult jobResult) {
        jobResult.setTicket(this.ticket);
        this.resultadoJobs.add(jobResult);
    }

    public void addJobResult(@NonNull Collection<JobResult> jobResults) {
        jobResults.forEach(job -> job.setTicket(this.ticket));
        this.resultadoJobs.addAll(jobResults);
    }

    public void addEvidenciaResult(@NonNull EvidenciaResult evidenciaResult) {
        evidenciaResult.evidencia().ifPresent(
            ev -> ev.setTicket(this.ticket)
        );
        this.resultadoEvidencias.add(evidenciaResult);
    }

    public void addEvidenciaResult(@NonNull Collection<EvidenciaResult> evidenciaResults) {
        evidenciaResults.forEach(
            result -> result.evidencia().ifPresent(ev -> ev.setTicket(this.ticket)
        ));
        this.resultadoEvidencias.addAll(evidenciaResults);
    }
}
