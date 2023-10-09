package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.master.MasterDtoResponse;
import lombok.*;

import java.util.List;

//@Data
@Builder
//@AllArgsConstructor
//@EqualsAndHashCode(callSuper = true)
public record PilhaEvidenciasResponseDTO(
    List<EvidenciaResponseDTO> evidencias
) implements MasterDtoResponse {}


