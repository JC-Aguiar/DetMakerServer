package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
//@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioInfoDTO implements MasterRequestDTO, MasterResponseDTO {

    String nomeAtividade;
    String nomeProjeto;
    String configuracao;

}
