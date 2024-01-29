package br.com.ppw.dma.configQuery;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigQueryBasicDTO {

    String tabelaNome;
    String sql;
    String descricao;

    public ConfigQueryBasicDTO(@NonNull ConfigQuery configQuery) {
        this.tabelaNome = configQuery.getTabelaNome();
        this.sql = configQuery.getSql();
        this.descricao = configQuery.getDescricao();
    }

}
