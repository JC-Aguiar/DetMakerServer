package br.com.ppw.dma.jobQuery;

import br.com.ppw.dma.job.Job;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class QueryInfoDTO {

    Optional<@Min(0) Long> id = Optional.empty();
    @NotNull @Min(0) Long jobId;
    @NotBlank String nome;
    String descricao = "";
    @NotBlank String sql;


    public QueryInfoDTO(JobQuery jobQuery) {
        this.id = Optional.of(jobQuery.getId());
        this.jobId = jobQuery.getJob().getId();
        atualizar(jobQuery);
    }

    private void atualizar(@NonNull JobQuery jobQuery) {
        this.nome = (jobQuery.getNome());
        this.descricao = (jobQuery.getDescricao());
        this.sql = (jobQuery.getSql());
    }

    public static List<QueryInfoDTO> getFromJob(@NonNull Job job) {
        return job.getQueries()
            .stream()
            .map(QueryInfoDTO::new)
            .toList();
    }

}
