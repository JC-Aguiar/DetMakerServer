package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.job.Job;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Optional;

@Valid
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueryInfoDTO extends ComandoSql {

    Optional<@Min(0) Long> id = Optional.empty();
    @NotNull @Min(0) Long jobId;


    public QueryInfoDTO(ConfigQuery configQuery) {
        this.id = Optional.of(configQuery.getId());
        this.jobId = configQuery.getJob().getId();
        atualizar(configQuery);
    }

//    public QueryInfoDTO(ComandoSql comandoSql) {
//        this.id = Optional.empty();
//        this.jobId = -1L;
//        atualizar(comandoSql);
//    }

    private void atualizar(@NonNull ConfigQuery configQuery) {
        setNome(configQuery.getNome());
        setDescricao(configQuery.getDescricao());
        setSql(configQuery.getSql());
        setFiltros(
            configQuery.getVariaveis()
                .stream()
                .map(FiltroSql::new)
                .toList()
        );
    }

    private void atualizar(@NonNull ComandoSql comandoSql) {
        setNome(comandoSql.getNome());
        setDescricao(comandoSql.getDescricao());
        setSql(comandoSql.getSql());
    }

    public static List<QueryInfoDTO> converterJobConfigQuery(@NonNull Job job) {
        return job.getQueries()
            .stream()
            .map(QueryInfoDTO::new)
            .toList();
    }

}
