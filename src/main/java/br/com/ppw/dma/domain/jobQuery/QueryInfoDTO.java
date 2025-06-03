package br.com.ppw.dma.domain.jobQuery;

import br.com.ppw.dma.domain.job.Job;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Optional;

import static lombok.AccessLevel.*;

@Data
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class QueryInfoDTO {

    Long id;
    Long jobId;
    String nome;
    String descricao;
    String sql;


    public QueryInfoDTO(JobQuery jobQuery) {
        this.id = jobQuery.getId();
        this.jobId = jobQuery.getJob().getId();
        atualizar(jobQuery);
    }

    private void atualizar(@NonNull JobQuery jobQuery) {
        this.nome = (jobQuery.getNome());
        this.descricao = (jobQuery.getDescricao());
        this.sql = (jobQuery.getSql());
    }

}
