package br.com.ppw.dma.domain.job;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Data
@Slf4j
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobParamDTO {

    @NotNull
    @Size(min = 1, max = 9)
    String tipo;

    @Size(min = 1, max = 30)
    String formato;

    @NotNull
    @Size(min = 1, max = 30)
    String nome;

    @Size(min = 1, max = 350)
    String descricao;

    @Builder.Default
    Boolean opcional = false;


    public JobParamDTO(@NonNull JobParameter param) {
        this.tipo = (param.getTipo().name());
        this.formato = (param.getFormato());
        this.nome = (param.getNome());
        this.descricao = (param.getDescricao());
        this.opcional = (param.getOpcional());
    }

    public JobParameter converterParaEntidade() {
        return JobParameter.builder()
            .tipo(JobParameterType.identify(tipo).orElse(null))
            .formato(formato)
            .nome(nome)
            .descricao(descricao)
            .opcional(opcional)
            .build();
    }

    public boolean getOpcional() {
        return Optional.ofNullable(opcional).orElse(false);
    }

}
