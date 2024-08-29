package br.com.ppw.dma.domain.cliente;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClienteNovoDTO {

    @NotBlank String nome;
    byte[] banner;

}
