package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.master.MasterResponseDTO;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

//@Data
//@FieldDefaults(level = AccessLevel.PRIVATE)
public record PipelineRelatorioDTO(
    @Getter @NotBlank String pipelineNome,
    @Getter @NotBlank String pipelineDescricao,
    @Getter RelatorioHistoricoDTO relatorio
) implements MasterResponseDTO {}
