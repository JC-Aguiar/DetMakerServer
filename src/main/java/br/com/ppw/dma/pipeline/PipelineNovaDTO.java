package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.relatorio.RelatorioInfoDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineNovaDTO implements MasterRequestDTO {

    PipelineInfoDTO pipeline;
    RelatorioInfoDTO relatorio;

}
