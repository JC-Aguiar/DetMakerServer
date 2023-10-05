package br.com.ppw.dma.job;

import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ComandoSql {

    private List<String> campos;
    private String tabela;
    private String filtro;

}
