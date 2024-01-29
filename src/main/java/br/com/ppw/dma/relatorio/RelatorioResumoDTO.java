package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.evidencia.Evidencia;
import lombok.NonNull;
import lombok.val;

import java.time.OffsetDateTime;

public record RelatorioResumoDTO(
    long id,
    long idPipeline,
    String nomePipeline,
    String idProjeto,
    String nomeProjeto,
    String nomeAtividade,
    OffsetDateTime data,
    boolean sucesso,
    int evidencias,
    int revisados
) {

    public static RelatorioResumoDTO converterEntidade(@NonNull Relatorio relatorio) {
        val pipeline = relatorio.getPipeline();
        val sucesso = relatorio.getSucesso() != null ? relatorio.getSucesso() : false;
        val evidencias = relatorio.getEvidencias();
        val revisados = evidencias.stream()
            .filter(Evidencia::jaRevisada)
            .toList()
            .size();

        return new RelatorioResumoDTO(
            relatorio.getId(),
            pipeline.getId(),
//            pipeline.getProps().getNome(),
            pipeline.getNome(),
            relatorio.getIdProjeto(),
            relatorio.getNomeProjeto(),
            relatorio.getNomeAtividade(),
            relatorio.getData(),
            sucesso,
            evidencias.size(),
            revisados);
    }

}
