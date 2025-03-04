package br.com.ppw.dma.domain.job;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.util.Optional;

@Data
@Slf4j
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobParamDTO {

    @NotNull
    @Length(max = 9)
    String tipo;

    @Length(max = 30)
    String formato;

    @NotNull
    @Length(max = 30)
    String nome;

    @Length(max = 350)
    String descricao;

    Boolean opcional;


    public static JobParamDTO converterEntidade(@NonNull JobParameter param) {
        return JobParamDTO.builder()
            .tipo(param.getTipo().name())
            .formato(param.getFormato())
            .nome(param.getNome())
            .descricao(param.getDescricao())
            .opcional(param.getOpcional())
            .build();

    }

    public boolean getOpcional() {
        return Optional.ofNullable(opcional).orElse(false);
    }

}
