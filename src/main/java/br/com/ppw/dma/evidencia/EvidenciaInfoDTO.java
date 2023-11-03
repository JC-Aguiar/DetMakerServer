package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.master.MasterResponseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class EvidenciaInfoDTO implements MasterResponseDTO {
    String job;
    @JsonIgnore String jobDescricao;
    @JsonIgnore OffsetDateTime data;
    Boolean sucesso;
    Integer ordem;
    String argumentos;
    List<String> queries;
    List<String> tabelasPreJob;
    List<String> tabelasPosJob;
    List<String> logs;
    List<String> logsNome;
    List<String> cargas;
    List<String> cargasNome;
    List<String> saidas;
    List<String> saidasNome;
}


