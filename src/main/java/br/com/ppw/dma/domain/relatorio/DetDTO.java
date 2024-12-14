package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.user.UserInfoDTO;
import lombok.NonNull;

import java.util.List;


public record DetDTO(
    @NonNull String pipelineNome,
    @NonNull String pipelineDescricao,
    @NonNull RelatorioHistoricoDTO relatorio,
    @NonNull List<UserInfoDTO> users)
{

    public static DetDTO from(Relatorio relatorio, List<UserInfoDTO> users) {
        if(relatorio.getPipelineNome() == null)
            throw new RuntimeException("Relatório sem relacionamento com uma Evidência");

        return new DetDTO(
//            relatorio.getPipeline().getProps().getNome(),
            relatorio.getPipelineNome(),
            relatorio.getPipelineDescricao(),
            new RelatorioHistoricoDTO(relatorio),
            users
        );
    }

}
