package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.master.MasterResponseDTO;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class EvidenciaInfoDTO implements MasterResponseDTO {
    String job;
    Boolean sucesso;
    Integer ordem;
    String argumentos;
    List<String> queries;
    List<String> tabelasPreJob;
    List<String> tabelasPosJob;
    List<String> logs;
    List<String> cargas;
    List<String> saidas;
}


