package br.com.ppw.dma.util;

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
    private String filtros;

    public String getSqlCompleta() {
        if(semTabela()) return "Tabela ainda não definida para esta instância";
        return "SELECT " + getSqlCampos() +
            " FROM " + tabela +
            getSqlFiltros();
    }

    public boolean semCampos() {
        return campos == null || campos.isEmpty();
    }

    public boolean semTabela() {
        return tabela == null || tabela.trim().isEmpty();
    }

    public boolean semFiltros() {
        return filtros == null || filtros.trim().isEmpty();
    }

    public String getSqlCampos() {
        return semCampos() ? " * " : String.join(", ", campos);
    }

    public String getSqlFiltros() {
        return semFiltros() ? "" : " WHERE " + filtros;
    }

}
