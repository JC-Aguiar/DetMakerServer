package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    Long id;
    @NotNull @Positive Long jobId;
    @NotBlank String nome;
    String descricao;
    @NotBlank String sql;
    List<FiltroSql> filtros = new ArrayList<>();
    Map<String, String> valores = new HashMap<>();

    public ComandoSql(@NonNull ExecQuery execQuery) {
        this.nome = execQuery.getQueryNome();
        this.descricao = "";
        this.sql = execQuery.getQuery();
//        this.filtros.addAll(FiltroSql.identificar(execQuery.getQuery()));
//        this.dinamico = false;
//        if(!filtros.isEmpty()) this.dinamico = true;
    }

    public List<FiltroSql> getFiltros() {
        return List.copyOf(filtros);
    }

    @JsonIgnore
    public Map<String, List<FiltroSql>> mapFiltrosPorTabela() {
        return filtros.stream().collect(
            Collectors.groupingBy(FiltroSql::getTabela)
        );
    }

    public boolean semFiltros() {
        return filtros.isEmpty();
    }

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
