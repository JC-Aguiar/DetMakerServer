package br.com.ppw.dma.domain.relatorio;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Valid
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioComplementoDTO {
    @NotNull @Min(0)             Long id;
    @NotBlank                    String idProjeto;
    @NotBlank                    String nomeProjeto;
    @NotBlank                    String nomeAtividade;
    @Nullable @Length(max = 500) String consideracoes;
}
