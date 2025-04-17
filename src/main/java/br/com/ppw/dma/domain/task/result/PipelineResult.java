package br.com.ppw.dma.domain.task.result;

import br.com.ppw.dma.domain.ambiente.Ambiente;
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
    final Ambiente ambiente;

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
    final List<JobProcess> resultadoJobs = new ArrayList<>();

    @Builder.Default
    final List<EvidenciaResult> resultadoEvidencias = new ArrayList<>();

    @Builder.Default
    boolean erro = false;


    public List<JobProcess> getResultadoJobs() {
        return List.copyOf(resultadoJobs);
    }

    public List<EvidenciaResult> getResultadoEvidencias() {
        return List.copyOf(resultadoEvidencias);
    }

    public void addJobResult(@NonNull JobProcess jobProcess) {
        jobProcess.setTicket(this.ticket);
        this.resultadoJobs.add(jobProcess);
    }

    public void addJobResult(@NonNull Collection<JobProcess> jobProcesses) {
        jobProcesses.forEach(job -> job.setTicket(this.ticket));
        this.resultadoJobs.addAll(jobProcesses);
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
