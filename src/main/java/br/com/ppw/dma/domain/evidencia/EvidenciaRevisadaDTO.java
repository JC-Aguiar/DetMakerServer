package br.com.ppw.dma.domain.evidencia;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class EvidenciaRevisadaDTO {

    Long evidenciaId;
    String resivor;
    OffsetDateTime dataRevisao;
    String comentario;
    String resultado;

}
