package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioHistoricoDTO implements MasterResponseDTO {

    String nomeAtividade;
    String nomeProjeto;
    String configuracao;
    List<EvidenciaInfoDTO> evidencias = new ArrayList<>();
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    Boolean sucesso;

}
