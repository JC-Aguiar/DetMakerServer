package br.com.ppw.dma.job;

import br.com.ppw.dma.job.ItemPilhaPostDTO.ComandoSqlPOJO;
import br.com.ppw.dma.batch.ShellPointer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemPilha<T extends ShellPointer> {

    Integer ordem;
    T registro;
    List<String> argumentos = new ArrayList<>();
    List<ComandoSqlPOJO> queries = new ArrayList<>();
    List<String> cargas = new ArrayList<>();
    Map<ComandoSqlPOJO, EvidenciaTabela> tabelasPreJob = new HashMap<>();
    Map<ComandoSqlPOJO, EvidenciaTabela> tabelasPosJob = new HashMap<>();
    List<File> logs = new ArrayList<>();
    List<File> entradas = new ArrayList<>();
    List<File> saidas = new ArrayList<>();
    boolean sucesso = false;

    public ItemPilha addTabelasPreJob(ComandoSqlPOJO comandoSql, EvidenciaTabela evidencia) {
        this.tabelasPreJob.put(comandoSql, evidencia);
        return this;
    }
    
    public ItemPilha addTabelasPreJob(Map<ComandoSqlPOJO, EvidenciaTabela> evidencia) {
        this.tabelasPreJob.putAll(evidencia);
        return this;
    }
    
    public ItemPilha addTabelasPosJob(ComandoSqlPOJO comandoSql, EvidenciaTabela evidencia) {
        this.tabelasPosJob.put(comandoSql, evidencia);
        return this;
    }
    
    public ItemPilha addTabelasPosJob(Map<ComandoSqlPOJO, EvidenciaTabela> evidencia) {
        this.tabelasPosJob.putAll(evidencia);
        return this;
    }

    public ItemPilha addArgumentos(String arg) {
        argumentos.add(arg);
        return this;
    }

    public ItemPilha addArgumentos(List<String> args) {
        argumentos.addAll(args);
        return this;
    }

    public ItemPilha limparArgumentos() {
        argumentos.clear();
        return this;
    }

    public ItemPilha addEvidencias(File arquivo) {
        logs.add(arquivo);
        return this;
    }

    public ItemPilha addEvidencias(List<File> arquivo) {
        logs.addAll(arquivo);
        return this;
    }

    public ItemPilha limparEvidencias() {
        logs.clear();
        return this;
    }

    public ItemPilha addEntradas(File arquivo) {
        entradas.add(arquivo);
        return this;
    }

    public ItemPilha addEntradas(List<File> arquivo) {
        entradas.addAll(arquivo);
        return this;
    }

    public ItemPilha limparEntradas() {
        entradas.clear();
        return this;
    }

    public ItemPilha addSaidas(File arquivo) {
        saidas.add(arquivo);
        return this;
    }

    public ItemPilha addSaidas(List<File> arquivo) {
        saidas.addAll(arquivo);
        return this;
    }

    public ItemPilha limparSaidas() {
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
