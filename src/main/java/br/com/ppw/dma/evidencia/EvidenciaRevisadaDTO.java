package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.master.MasterRequestDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class EvidenciaRevisadaDTO implements MasterRequestDTO {

    Long evidenciaId;
    String resivor;
    OffsetDateTime dataRevisao;
    String comentario;
    String resultado;

}
