package br.com.ppw.dma.relatorio;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;

//@Data
//@FieldDefaults(level = AccessLevel.PRIVATE)
public record DetDTO(
    @Getter @NotBlank String pipelineNome,
    @Getter @NotBlank String pipelineDescricao,
    @Getter RelatorioHistoricoDTO relatorio
) {

    public static DetDTO from(@NonNull Relatorio relatorio) {
        if(relatorio.getPipeline() == null)
            throw new RuntimeException("Relatório sem relacionamento com uma Evidência");

        return new DetDTO(
            relatorio.getPipeline().getProps().getNome(),
            relatorio.getPipeline().getDescricao(),
            new RelatorioHistoricoDTO(relatorio)
        );
    }

}
