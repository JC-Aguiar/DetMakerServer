package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
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
public class ComandoSql implements MasterRequestDTO, MasterResponseDTO {

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
