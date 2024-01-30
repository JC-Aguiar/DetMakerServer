package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.execQuery.ExecQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComandoSql {

    final List<String> campos= new ArrayList<>();
    String tabela;
    final List<FiltroSql> filtros = new ArrayList<>();
    String sql;
    String descricao;
    boolean dinamico;

    public static final int ROWNUM_LIMIT = 50;


    public ComandoSql(@NotBlank String tabela) {
        this.tabela = tabela;
    }

    public ComandoSql(@NonNull ConfigQuery configQuery) {
        this.tabela = configQuery.getTabelaNome();
        this.filtros.addAll(FiltroSql.identificar(configQuery.getSql()));
        this.sql = configQuery.getSql();
        this.descricao = configQuery.getDescricao();
        if(!filtros.isEmpty()) this.dinamico = true;
    }

    public ComandoSql(@NonNull ExecQuery execQuery) {
        this.tabela = execQuery.getTabelaNome();
        this.filtros.addAll(FiltroSql.identificar(execQuery.getQuery()));
        this.sql = execQuery.getQuery();
        this.descricao = "";
        if(!filtros.isEmpty()) this.dinamico = true;
    }

    @JsonIgnore
    public ComandoSql addCampo(@NotBlank String campo) {
        this.campos.add(campo);
        return this;
    }

    @JsonIgnore
    public ComandoSql addCampo(@NotEmpty List<String> campos) {
        this.campos.addAll(campos);
        return this;
    }

    @JsonIgnore
    public String getSqlCompleta() {
        if(semTabela()) throw new RuntimeException("Tabela não definida.");
        if(!dinamico) return sql;
        if(filtros.isEmpty())
            throw new RuntimeException("Não existem filtros declarados para se montar o SQL.");

        val valoresPendentes = filtros.stream()
            .map(FiltroSql::getValor)
            .anyMatch(valor -> valor == null || valor.isEmpty());
        if(valoresPendentes) throw new RuntimeException("Favor preencher todos os campos antes da query.");

        return FiltroSql.montarSql(sql, filtros);
    }

    @JsonIgnore
    public boolean semTabela() {
        return tabela == null || tabela.trim().isEmpty();
    }

    @JsonIgnore
    public boolean semFiltros() {
        return filtros.isEmpty();
    }

}
