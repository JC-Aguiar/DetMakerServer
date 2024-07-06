package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.job.Job;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueryInfoDTO {

    @NotNull Long id;
    @NotNull String nome;
    @NotNull String descricao;
    @NotNull String sql;


    public QueryInfoDTO(@NonNull ConfigQuery configQuery) {
        this.id = configQuery.getId();
        this.nome = configQuery.getNome();
        this.descricao = configQuery.getDescricao();
        this.sql = configQuery.getSql();
    }

    public QueryInfoDTO(@NonNull ComandoSql comandoSql) {
        this.id = -1L;
        this.nome = comandoSql.getNome();
        this.descricao = comandoSql.getDescricao();
        this.sql = comandoSql.getSql();
    }

    public QueryInfoDTO(@NonNull ExecQuery execQuery) {
        this.id = -1L;
        this.nome = execQuery.getQueryNome();
        this.descricao = "";
        this.sql = execQuery.getQuery();
//        job.getQueries()
//            .stream()
//            .filter(configQuery -> configQuery.getNome().equals(this.nome))
//            .findFirst()
//            .ifPresent(configQuery -> {
//                this.id = configQuery.getId();
//                this.descricao = configQuery.getDescricao();
//            });
    }

    public static List<QueryInfoDTO> converterJobConfigQuery(@NonNull Job job) {
        return job.getQueries()
            .stream()
            .map(QueryInfoDTO::new)
            .toList();
    }

}
