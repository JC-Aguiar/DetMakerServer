package br.com.ppw.dma.domain.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobResourceDTO {

    @NotNull
    @Length(max = 9)
    String tipo;

    @NotNull
    @Length(max = 150)
    String diretorio;

    @NotNull
    @Length(max = 150)
    String mascara;

    @Length(max = 350)
    String descricao;

    @NotNull
    @Length(max = 9)
    String acesso;


    public static JobResourceDTO converterEntidade(@NonNull JobResource resource) {
        return JobResourceDTO.builder()
            .tipo(resource.getTipo().name())
            .diretorio(resource.getDiretorio())
            .mascara(resource.getMascara())
            .descricao(resource.getDescricao())
            .acesso(resource.getAcesso().name())
            .build();

    }

    @JsonIgnore
    public String getAbsolutePath() {
        if(!diretorio.endsWith("/"))
            return diretorio + "/" + mascara;
        return diretorio + mascara;
    }

}
