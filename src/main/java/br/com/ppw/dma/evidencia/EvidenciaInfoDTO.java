package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.master.MasterResponseDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EvidenciaInfoDTO implements MasterResponseDTO {

    Long id;
    String job;
    @JsonIgnore String jobDescricao;
    @JsonIgnore OffsetDateTime data;
    Boolean sucesso;
    Integer ordem;
    String argumentos;
    List<String> queries;
    List<String> tabelasNome;
    List<String> tabelasPreJob;
    List<String> tabelasPosJob;
    List<AnexoInfoDTO> logs;
    List<AnexoInfoDTO> cargas;
    List<AnexoInfoDTO> saidas;

    public List<String> getTabelas() {
        final List<String> listaFinal = new ArrayList<>();
        listaFinal.addAll(tabelasPreJob);
        listaFinal.addAll(tabelasPosJob);
        return listaFinal;
    }

    public List<AnexoInfoDTO> getAnexos() {
        final List<AnexoInfoDTO> listaFinal = new ArrayList<>();
        listaFinal.addAll(logs);
        listaFinal.addAll(cargas);
        listaFinal.addAll(saidas);
        return listaFinal;
    }

    @Override
    public String toString() {
        return "EvidenciaInfoDTO(" +
            "job='" + job + '\'' +
            ", jobDescricao='" + jobDescricao + '\'' +
            ", data=" + data +
            ", sucesso=" + sucesso +
            ", ordem=" + ordem +
            ", argumentos='" + argumentos + '\'' +
            ", queries=" + queries +
            ", tabelasPreJob=" + getResumoTabelasPreJob() +
            ", tabelasPosJob=" + getResumoTabelasPosJob() +
            ", logs=" + getResumoLogs() +
            ", cargas=" + cargas +
            ", saidas=" + saidas +
            ')';
    }

    private String getResumoTabelasPreJob() {
        val tamanho = tabelasPreJob.size();
        val peso = tabelasPreJob.stream()
            .map(String::getBytes)
            .mapToLong(bytes -> bytes.length)
            .sum();
        return String.format("[registros=%d, peso=%dKbs]", tamanho, peso);
    }

    private String getResumoTabelasPosJob() {
        val tamanho = tabelasPosJob.size();
        val peso = tabelasPosJob.stream()
            .map(String::getBytes)
            .mapToLong(bytes -> bytes.length)
            .sum();
        return String.format("[registros=%d, peso=%dKbs]", tamanho, peso);
    }

    private String getResumoLogs() {
        return getResumo(logs);
    }

    private String getResumoCargas() {
        return getResumo(cargas);
    }

    private String getResumoSaidas() {
        return getResumo(saidas);
    }

    private String getResumo(@NonNull List<AnexoInfoDTO> anexos) {
        val tamanho = anexos.size();
        val peso = anexos.stream()
                .mapToLong(AnexoInfoDTO::getPeso)
                .sum();
        return String.format("[quantidade=%d, peso=%dKbs]", tamanho, peso);
    }
}


