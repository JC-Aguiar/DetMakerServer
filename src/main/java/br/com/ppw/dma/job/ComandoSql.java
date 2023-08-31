package br.com.ppw.dma.job;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ComandoSql {
    private String tabela;
    private String query;
}
