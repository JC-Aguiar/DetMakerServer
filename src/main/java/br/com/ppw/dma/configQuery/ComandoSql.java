package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComandoSql implements Serializable {

    @NotBlank String nome;

    String descricao;

    @NotBlank String sql;

    List<FiltroSql> filtros = new ArrayList<>();

    @JsonIgnore
    Map<String, String> valores = new HashMap<>();


    public ComandoSql(@NonNull ExecQuery execQuery) {
        this.nome = execQuery.getQueryNome();
        this.descricao = "";
        this.sql = execQuery.getQuery();
//        this.filtros.addAll(FiltroSql.identificar(execQuery.getQuery()));
//        this.dinamico = false;
//        if(!filtros.isEmpty()) this.dinamico = true;
    }

    public ComandoSql(@NonNull ConfigQuery configQuery) {
        this.nome = configQuery.getNome();
        this.descricao = configQuery.getDescricao();
        this.sql = configQuery.getSql();
        this.filtros.addAll(
            configQuery.getVariaveis()
                .stream()
                .map(FiltroSql::new)
                .toList());
//        this.dinamico = false;
//        if(!filtros.isEmpty()) this.dinamico = true;
    }

    @JsonIgnore
    public Map<String, List<FiltroSql>> groupFiltrosPorTabela() {
        return filtros.parallelStream().collect(
            Collectors.groupingBy(FiltroSql::getTabela)
        );
    }

    public boolean semFiltros() {
        return filtros.isEmpty();
    }

    @JsonIgnore
    public String getSqlCompleta() {
        return FormatString.substituirVariaveis(sql, getValores());
    }

//    public void validarFiltrosPreenchidos() throws NoSuchAttributeException {
//        var valoresPendentes = getFiltros()
//            .stream()
//            .anyMatch(filtro -> filtro.getValor().isBlank());
//        if(valoresPendentes) throw new NoSuchAttributeException("Existem filtros sem valor definido");
//    }
}
