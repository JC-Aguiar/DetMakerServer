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
    @ToString.Exclude List<String> tabelasPreJob = new ArrayList<>();
    @ToString.Exclude List<String> tabelasPosJob = new ArrayList<>();
    List<String> queriesInconformidade = new ArrayList<>();
    @ToString.Exclude List<AnexoInfoDTO> cargas = new ArrayList<>();
    @ToString.Exclude List<AnexoInfoDTO> logs = new ArrayList<>();
    @ToString.Exclude List<AnexoInfoDTO> saidas = new ArrayList<>();
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
        log.info("Convertendo Evidencia Nº{} em {}.", ordem, EvidenciaInfoDTO.class.getSimpleName());
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
        this.job = evidencia.getJobNome();
        this.jobDescricao = evidencia.getJobDescricao();
        this.argumentos = evidencia.getParametros();
        this.queries = queries;
        this.queriesNome = queriesNome;
        this.tabelasPreJob = bancoPreJob;
        this.tabelasPosJob = bancoPosJob;
        this.cargas = AnexoInfoDTO.converterExecFile(evidencia.getCargas());
        this.logs = AnexoInfoDTO.converterExecFile(evidencia.getLogs());
        this.saidas = AnexoInfoDTO.converterExecFile(evidencia.getRemessas());
//        this.cargas.forEach(c -> inconformidades.add(c.inconformidade()));
//        this.logs.forEach(l -> inconformidades.add(l.inconformidade()));
//        this.saidas.forEach(s -> inconformidades.add(s.inconformidade()));
        this.exitCode = evidencia.getExitCode();
        this.sha256 = evidencia.getVersao();
        this.erroFatal = evidencia.getMensagemErro();
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
        if(evidencia.getStatus() != null)
            this.resultado = evidencia.getStatus().status;
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

    @ToString.Include(name = "tabelasPreJob")
    public String getResumoTabelasPreJob() {
        return String.format("[registros=%d]", tabelasPreJob.size());
    }

    @ToString.Include(name = "tabelasPosJob")
    public String getResumoTabelasPosJob() {
        return String.format("[registros=%d]", tabelasPosJob.size());
    }

    @ToString.Include(name = "logs")
    public String getResumoLogs() {
        return getResumo(logs);
    }

    @ToString.Include(name = "cargas")
    public String getResumoCargas() {
        return getResumo(cargas);
    }

    @ToString.Include(name = "saidas")
    public String getResumoSaidas() {
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


