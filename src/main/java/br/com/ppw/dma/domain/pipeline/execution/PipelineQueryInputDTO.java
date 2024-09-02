package br.com.ppw.dma.domain.pipeline.execution;

import br.com.ppw.dma.domain.execQuery.ExecQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Valid
@Data
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class PipelineQueryInputDTO {

    Optional<String> nome = Optional.empty();
    Optional<String> descricao = Optional.empty();
    @NotBlank String sql;


    public PipelineQueryInputDTO(@NonNull ExecQuery execQuery) {
        this.nome = Optional.of(execQuery.getQueryNome());
        this.sql = execQuery.getQuery();
    }

}
