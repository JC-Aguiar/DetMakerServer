package br.com.ppw.dma.evidencia;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class EvidenciaInfoDTO {

    Long id;
    String job;
    String jobDescricao;
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    Long duracao;
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
    String analise;
    String revisor;
    OffsetDateTime dataRevisao;
    String requisitos;
    String comentario;
    String resultado;

    public EvidenciaInfoDTO(@NonNull Evidencia evidencia) {
        this(evidencia, evidencia.getOrdem());
    }

    public EvidenciaInfoDTO(@NonNull Evidencia evidencia, @NonNull Integer ordem) {
        log.info("Convertendo Evidencia em EvidenciaInfoDTO.");
        log.info("Ordem da evidência na Pipeline: {}.", ordem);
        val queries = new ArrayList<String>();
        val tabelasNome = new ArrayList<String>();
        val bancoPreJob = new ArrayList<String>();
        val bancoPosJob = new ArrayList<String>();
        for(val execQuery : evidencia.getBanco()) {
            queries.add(execQuery.getQuery());
            tabelasNome.add(execQuery.getTabelaNome());
            bancoPreJob.add(execQuery.getResultadoPreJob());
            bancoPosJob.add(execQuery.getResultadoPosJob());
        }
        this.id = evidencia.getId();
        this.job = evidencia.getJob().getNome();
        this.jobDescricao = evidencia.getJob().getDescricao();
        this.dataInicio = evidencia.getDataInicio();
        this.dataFim = evidencia.getDataFim();
        if(this.dataInicio != null && this.dataFim != null) {
            this.duracao = Duration
                .between(dataInicio.toInstant(), dataFim.toInstant())
                .getSeconds() * 1000;
        }
        this.sucesso = evidencia.getSucesso();
        this.ordem = ordem;
        this.argumentos = evidencia.getArgumentos();
        this.queries = queries;
        this.tabelasNome = tabelasNome;
        this.tabelasPreJob = bancoPreJob;
        this.tabelasPosJob = bancoPosJob;
        this.cargas = AnexoInfoDTO.converterExecFile(evidencia.getCargas());
        this.logs = AnexoInfoDTO.converterExecFile(evidencia.getLogs());
        this.saidas = AnexoInfoDTO.converterExecFile(evidencia.getSaidas());
        this.analise = evidencia.getAnalise();
        this.revisor = evidencia.getRevisor();
        this.dataRevisao = evidencia.getDataRevisao();
        this.requisitos = evidencia.getRequisitos();
        this.comentario = evidencia.getComentario();
        if(evidencia.getResultado() != null)
            this.resultado = evidencia.getResultado().status;

        log.info(this.toString());
    }

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
            ", dataInicio=" + dataInicio +
            ", dataFim=" + dataFim +
            ", duracao=" + duracao +
            ", sucesso=" + sucesso +
            ", ordem=" + ordem +
            ", argumentos='" + argumentos + '\'' +
            ", queries=" + queries +
            ", tabelasPreJob=" + getResumoTabelasPreJob() +
            ", tabelasPosJob=" + getResumoTabelasPosJob() +
            ", logs=" + getResumoLogs() +
            ", cargas=" + getResumoCargas() +
            ", saidas=" + getResumoSaidas() +
            ", resivor='" + revisor + '\'' +
            ", dataRevisao=" + dataRevisao +
            ", requisitos='" + requisitos + '\'' +
            ", expectativa='" + comentario + '\'' +
            ", resultado='" + resultado + '\'' +
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


