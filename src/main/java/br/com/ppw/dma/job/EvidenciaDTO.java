package br.com.ppw.dma.job;

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
public class EvidenciaDTO {

    Integer ordem;
    AgendaID id;
    String job;
    List<String> argumentos = new ArrayList<>();
    List<ComandoSql> queries = new ArrayList<>();
    List<String> cargas = new ArrayList<>();
    Map<ComandoSql, EvidenciaTabela> tabelasPreJob = new HashMap<>();
    Map<ComandoSql, EvidenciaTabela> tabelasPosJob = new HashMap<>();
    List<File> logs = new ArrayList<>();
    List<File> entradas = new ArrayList<>();
    List<File> saidas = new ArrayList<>();
    boolean sucesso = false;

    public EvidenciaDTO addTabelasPreJob(ComandoSql comandoSql, EvidenciaTabela evidencia) {
        this.tabelasPreJob.put(comandoSql, evidencia);
        return this;
    }
    
    public EvidenciaDTO addTabelasPreJob(Map<ComandoSql, EvidenciaTabela> evidencia) {
        this.tabelasPreJob.putAll(evidencia);
        return this;
    }
    
    public EvidenciaDTO addTabelasPosJob(ComandoSql comandoSql, EvidenciaTabela evidencia) {
        this.tabelasPosJob.put(comandoSql, evidencia);
        return this;
    }
    
    public EvidenciaDTO addTabelasPosJob(Map<ComandoSql, EvidenciaTabela> evidencia) {
        this.tabelasPosJob.putAll(evidencia);
        return this;
    }

    public EvidenciaDTO addArgumentos(String arg) {
        argumentos.add(arg);
        return this;
    }

    public EvidenciaDTO addArgumentos(List<String> args) {
        argumentos.addAll(args);
        return this;
    }

    public EvidenciaDTO limparArgumentos() {
        argumentos.clear();
        return this;
    }

    public EvidenciaDTO addEvidencias(File arquivo) {
        logs.add(arquivo);
        return this;
    }

    public EvidenciaDTO addEvidencias(List<File> arquivo) {
        logs.addAll(arquivo);
        return this;
    }

    public EvidenciaDTO limparEvidencias() {
        logs.clear();
        return this;
    }

    public EvidenciaDTO addEntradas(File arquivo) {
        entradas.add(arquivo);
        return this;
    }

    public EvidenciaDTO addEntradas(List<File> arquivo) {
        entradas.addAll(arquivo);
        return this;
    }

    public EvidenciaDTO limparEntradas() {
        entradas.clear();
        return this;
    }

    public EvidenciaDTO addSaidas(File arquivo) {
        saidas.add(arquivo);
        return this;
    }

    public EvidenciaDTO addSaidas(List<File> arquivo) {
        saidas.addAll(arquivo);
        return this;
    }

    public EvidenciaDTO limparSaidas() {
        saidas.clear();
        return this;
    }
}
