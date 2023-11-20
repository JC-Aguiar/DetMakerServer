package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigQueryBasicDTO implements MasterRequestDTO, MasterResponseDTO {

    String tabelaNome;
    String sql;
    String descricao;

    public ConfigQueryBasicDTO(@NonNull ConfigQuery configQuery) {
        this.tabelaNome = configQuery.getTabelaNome();
        this.sql = configQuery.getSql();
        this.descricao = configQuery.getDescricao();
    }

}
