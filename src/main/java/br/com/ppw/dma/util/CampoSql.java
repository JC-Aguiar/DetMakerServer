package br.com.ppw.dma.util;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampoSql {

    String nome;
    Object valor;

    @Override
    public String toString() {
        return nome + ": " + valor;
    }
}
