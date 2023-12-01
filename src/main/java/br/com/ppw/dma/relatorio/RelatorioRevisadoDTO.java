package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.master.MasterRequestDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioRevisadoDTO implements MasterRequestDTO {
    Long id;
    String consideracoes;
}
