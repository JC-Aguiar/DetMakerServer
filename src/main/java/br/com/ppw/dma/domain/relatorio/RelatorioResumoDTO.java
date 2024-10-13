package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import lombok.Builder;
import lombok.NonNull;
import lombok.val;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Predicate;

@Builder
public record RelatorioResumoDTO(

    long id,
    String ticket,
    String nomePipeline,
    String idProjeto,
    String nomeProjeto,
    String nomeAtividade,
    String autor,
    OffsetDateTime data,
    boolean erro,
    int evidencias,
    int revisados) {


    public static RelatorioResumoDTO converterEntidade(@NonNull Relatorio relatorio) {
        val erro = Optional.ofNullable(relatorio.getSucesso())
            .map(sucesso -> !sucesso)
            .orElse(false);
        val evidencias = relatorio.getEvidencias();
        val revisados = evidencias.stream()
            .filter(Evidencia::jaRevisada)
            .toList()
            .size();
        return RelatorioResumoDTO.builder()
            .id(relatorio.getId())
            .ticket(relatorio.getTicket())
            .nomePipeline(relatorio.getPipelineNome())
            .idProjeto(relatorio.getIdProjeto())
            .nomeProjeto(relatorio.getNomeProjeto())
            .nomeAtividade(relatorio.getNomeAtividade())
            .autor(relatorio.getUsuario())
            .data(relatorio.getDataCompleta())
            .erro(erro)
            .evidencias(evidencias.size())
            .revisados(revisados)
            .build();
    }

}
