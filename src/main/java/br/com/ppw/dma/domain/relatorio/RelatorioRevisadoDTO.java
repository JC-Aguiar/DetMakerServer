package br.com.ppw.dma.domain.relatorio;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioRevisadoDTO {
    Long id;
    String consideracoes;
}
