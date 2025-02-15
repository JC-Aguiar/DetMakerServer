package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.execFile.AnexoInfoDTO;
import br.com.ppw.dma.domain.execQuery.ExecQueryDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    String comandoExec;
//    List<String> queriesNome = new ArrayList<>();
//    @ToString.Exclude List<String> tabelasPreJob = new ArrayList<>();
//    @ToString.Exclude List<String> tabelasPosJob = new ArrayList<>();
//    List<String> queriesInconformidade = new ArrayList<>();
    @ToString.Exclude List<ExecQueryDTO> queries = new ArrayList<>();
    @ToString.Exclude List<AnexoInfoDTO> cargas = new ArrayList<>();
    @ToString.Exclude List<AnexoInfoDTO> logs = new ArrayList<>();
    @ToString.Exclude List<AnexoInfoDTO> remessas = new ArrayList<>();
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
        this.id = evidencia.getId();
        this.ordem = ordem;
        this.job = evidencia.getJobNome();
        this.jobDescricao = evidencia.getJobDescricao();
        this.comandoExec = evidencia.getComandoExec();
        this.queries = evidencia.getQueries()
            .stream()
            .map(ExecQueryDTO::new)
            .toList();
//        this.queriesNome = queriesNome;
//        this.tabelasPreJob = bancoPreJob;
//        this.tabelasPosJob = bancoPosJob;
        this.cargas = AnexoInfoDTO.converterExecFile(evidencia.getCargas());
        this.logs = AnexoInfoDTO.converterExecFile(evidencia.getLogs());
        this.remessas = AnexoInfoDTO.converterExecFile(evidencia.getRemessas());
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

    @JsonIgnore
    public Set<String> getAllResutlados() {
        return Stream.concat(
            queries.stream().map(ExecQueryDTO::getTabelaPreJob),
            queries.stream().map(ExecQueryDTO::getTabelaPosJob)
        ).collect(Collectors.toSet());
    }

    @JsonIgnore
    public List<AnexoInfoDTO> getAnexos() {
        final List<AnexoInfoDTO> listaFinal = new ArrayList<>();
        listaFinal.addAll(logs);
        listaFinal.addAll(cargas);
        listaFinal.addAll(remessas);
        return listaFinal;
    }

    @JsonIgnore
    @ToString.Include(name = "logs")
    public String getResumoLogs() {
        return getResumo(logs);
    }

    @JsonIgnore
    @ToString.Include(name = "cargas")
    public String getResumoCargas() {
        return getResumo(cargas);
    }

    @JsonIgnore
    @ToString.Include(name = "remessas")
    public String getResumoRemessas() {
        return getResumo(remessas);
    }

    private String getResumo(@NonNull List<AnexoInfoDTO> anexos) {
        val tamanho = anexos.size();
        val peso = anexos.stream()
            .mapToLong(AnexoInfoDTO::getPeso)
            .sum();
        return String.format("[quantidade=%d, peso=%dKbs]", tamanho, peso);
    }


}


