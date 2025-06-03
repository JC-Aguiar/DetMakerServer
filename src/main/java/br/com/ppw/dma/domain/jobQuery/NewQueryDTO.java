package br.com.ppw.dma.domain.jobQuery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class NewQueryDTO {

    @NotBlank(message = "O nome da query deve ser definido")
    @Size(max = 50, message = "Tamanho máximo do nome da query é de 50 caracteres")
    String nome;

    @Size(max = 500, message = "Tamanho máximo da descrição da query é de 500 caracteres")
    String descricao = "";

    @NotBlank(message = "A query deve ser definida")
    @Size(max = 900, message = "Tamanho máximo da query é de 900 caracteres")
    String sql;


    public JobQuery toEntity() {
        return JobQuery.builder()
            .nome(nome)
            .descricao(descricao)
            .sql(sql)
            .build();
    }

}
