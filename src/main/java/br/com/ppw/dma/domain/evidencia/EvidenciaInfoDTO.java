package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.execFile.AnexoInfoDTO;
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
public class EvidenciaInfoDTO {

    Long id;
    Integer ordem;
    String job;
    String jobDescricao;
    String argumentos;
    List<String> queries = new ArrayList<>();
    List<String> queriesNome = new ArrayList<>();
    List<String> tabelasPreJob = new ArrayList<>();
    List<String> tabelasPosJob = new ArrayList<>();
    List<String> queriesInconformidade = new ArrayList<>();
    List<AnexoInfoDTO> cargas = new ArrayList<>();
    List<AnexoInfoDTO> logs = new ArrayList<>();
    List<AnexoInfoDTO> saidas = new ArrayList<>();
    Integer exitCode;
    String sha256;
    String erroFatal;
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    Long duracao;
    String revisor;
    OffsetDateTime dataRevisao;
    String requisitos;
    String comentario;
    String resultado;

    public EvidenciaInfoDTO(@NonNull Evidencia evidencia) {
        this(evidencia, evidencia.getOrdem());
    }

    public EvidenciaInfoDTO(@NonNull Evidencia evidencia, @NonNull Integer ordem) {
        log.info("Convertendo Evidencia em {}.", EvidenciaInfoDTO.class.getSimpleName());
        log.info("Ordem da evidência na Pipeline: {}.", ordem);
        val queries = new ArrayList<String>();
        val queriesNome = new ArrayList<String>();
        val bancoPreJob = new ArrayList<String>();
        val bancoPosJob = new ArrayList<String>();
        for(val execQuery : evidencia.getQueries()) {
            queries.add(execQuery.getQuery());
            queriesNome.add(execQuery.getQueryNome());
            bancoPreJob.add(execQuery.getResultadoPreJob());
            bancoPosJob.add(execQuery.getResultadoPosJob());
            this.queriesInconformidade.add(execQuery.getInconformidade());
        }
        this.id = evidencia.getId();
        this.ordem = ordem;
        this.job = evidencia.getJob().getNome();
        this.jobDescricao = evidencia.getJob().getDescricao();
        this.argumentos = evidencia.getArgumentos();
        this.queries = queries;
        this.queriesNome = queriesNome;
        this.tabelasPreJob = bancoPreJob;
        this.tabelasPosJob = bancoPosJob;
        this.cargas = AnexoInfoDTO.converterExecFile(evidencia.getCargas());
        this.logs = AnexoInfoDTO.converterExecFile(evidencia.getLogs());
        this.saidas = AnexoInfoDTO.converterExecFile(evidencia.getSaidas());
//        this.cargas.forEach(c -> inconformidades.add(c.inconformidade()));
//        this.logs.forEach(l -> inconformidades.add(l.inconformidade()));
//        this.saidas.forEach(s -> inconformidades.add(s.inconformidade()));
        this.exitCode = evidencia.getExitCode();
        this.sha256 = evidencia.getSha256();
        this.erroFatal = evidencia.getErroFatal();
        this.dataInicio = evidencia.getDataInicio();
        this.dataFim = evidencia.getDataFim();
        if(this.dataInicio != null && this.dataFim != null) {
            this.duracao = Duration
                .between(dataInicio.toInstant(), dataFim.toInstant())
                .getSeconds() * 1000;
        }
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
        return "EvidenciaInfoDTO{" +
            "id=" + id +
            ", ordem=" + ordem +
            ", job='" + job + '\'' +
            ", jobDescricao='" + jobDescricao + '\'' +
            ", argumentos='" + argumentos + '\'' +
            ", queries=" + queries +
            ", queriesNome=" + queriesNome +
            ", tabelasPreJob=" + getResumoTabelasPreJob() +
            ", tabelasPosJob=" + getResumoTabelasPosJob() +
            ", cargas=" + getResumoCargas() +
            ", logs=" + getResumoLogs() +
            ", saidas=" + getResumoSaidas() +
            ", exitCode=" + exitCode +
            ", sha256='" + sha256 + '\'' +
            ", erroFatal='" + erroFatal + '\'' +
            ", dataInicio=" + dataInicio +
            ", dataFim=" + dataFim +
            ", duracao=" + duracao +
            ", revisor='" + revisor + '\'' +
            ", dataRevisao=" + dataRevisao +
            ", requisitos='" + requisitos + '\'' +
            ", comentario='" + comentario + '\'' +
            ", resultado='" + resultado + '\'' +
            '}';
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


