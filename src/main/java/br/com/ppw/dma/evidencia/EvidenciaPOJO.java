package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.job.JobDTO;
import br.com.ppw.dma.job.ComandoSql;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvidenciaPOJO {

    Long id;
    Integer ordem;
    JobDTO registro;
    List<String> argumentos = new ArrayList<>();
    List<ComandoSql> queries = new ArrayList<>();
    List<String> cargas = new ArrayList<>();
    Map<ComandoSql, ExtrcaoBanco> tabelasPreJob = new HashMap<>();
    Map<ComandoSql, ExtrcaoBanco> tabelasPosJob = new HashMap<>();
    List<File> logs = new ArrayList<>();
    List<File> entradas = new ArrayList<>();
    List<File> saidas = new ArrayList<>();
    boolean sucesso = false;

    public EvidenciaPOJO addTabelasPreJob(ComandoSql comandoSql, ExtrcaoBanco evidencia) {
        this.tabelasPreJob.put(comandoSql, evidencia);
        return this;
    }

    public EvidenciaPOJO addTabelasPreJob(Map<ComandoSql, ExtrcaoBanco> evidencia) {
        this.tabelasPreJob.putAll(evidencia);
        return this;
    }

    public EvidenciaPOJO addTabelasPosJob(ComandoSql comandoSql, ExtrcaoBanco evidencia) {
        this.tabelasPosJob.put(comandoSql, evidencia);
        return this;
    }

    public EvidenciaPOJO addTabelasPosJob(Map<ComandoSql, ExtrcaoBanco> evidencia) {
        this.tabelasPosJob.putAll(evidencia);
        return this;
    }

    public EvidenciaPOJO addArgumentos(String arg) {
        argumentos.add(arg);
        return this;
    }

    public EvidenciaPOJO addArgumentos(List<String> args) {
        argumentos.addAll(args);
        return this;
    }

    public EvidenciaPOJO limparArgumentos() {
        argumentos.clear();
        return this;
    }

    public EvidenciaPOJO addEvidencias(File arquivo) {
        logs.add(arquivo);
        return this;
    }

    public EvidenciaPOJO addEvidencias(List<File> arquivo) {
        logs.addAll(arquivo);
        return this;
    }

    public EvidenciaPOJO limparEvidencias() {
        logs.clear();
        return this;
    }

    public EvidenciaPOJO addEntradas(File arquivo) {
        entradas.add(arquivo);
        return this;
    }

    public EvidenciaPOJO addEntradas(List<File> arquivo) {
        entradas.addAll(arquivo);
        return this;
    }

    public EvidenciaPOJO limparEntradas() {
        entradas.clear();
        return this;
    }

    public EvidenciaPOJO addSaidas(File arquivo) {
        saidas.add(arquivo);
        return this;
    }

    public EvidenciaPOJO addSaidas(List<File> arquivo) {
        saidas.addAll(arquivo);
        return this;
    }

    public EvidenciaPOJO limparSaidas() {
        saidas.clear();
        return this;
    }

    public String getParametro() {
        return String.join(" ", argumentos);
    }

    public String comandoShell() {
        return "ksh "
            .concat(registro.pathShell())
            .concat(" ")
            .concat(getParametro());
    }

    public String pathLog() {
        return "ksh "
            .concat(registro.pathShell())
            .concat(" ")
            .concat(getParametro());
    }
}
