package br.com.ppw.dma.job;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class ComandoSql {

    private List<String> campos;
    private String tabela;
    private String filtro;

}
