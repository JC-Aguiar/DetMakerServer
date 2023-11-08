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
                ", logsNome=" + logsNome +
                ", cargas=" + cargas +
                ", cargasNome=" + cargasNome +
                ", saidas=" + saidas +
                ", saidasNome=" + saidasNome +
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
        val tamanho = logs.size();
        val peso = logs.stream()
            .map(String::getBytes)
            .mapToLong(bytes -> bytes.length)
            .sum();
        return String.format("[quantidade=%d, peso=%dKbs]", tamanho, peso);
    }
}


