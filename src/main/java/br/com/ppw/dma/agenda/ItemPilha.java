package br.com.ppw.dma.agenda;

import br.com.ppw.dma.batch.ShellPointer;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ItemPilha<T extends ShellPointer> {

    private final List<String> argumentos = new ArrayList<>();
    private final T registro;
    private final Map<String, EvidenciaTabela> tabelasPreJob = new HashMap<>();
    private final Map<String, EvidenciaTabela> tabelasPosJob = new HashMap<>();
    private final List<File> logs = new ArrayList<>();
    private final List<File> entradas = new ArrayList<>();
    private final List<File> saidas = new ArrayList<>();
    private boolean sucesso = false;
    
    public ItemPilha addTabelasPreJob(String tabela, EvidenciaTabela evidencia) {
        this.tabelasPreJob.put(tabela, evidencia);
        return this;
    }
    
    public ItemPilha addTabelasPreJob(Map<String, EvidenciaTabela> evidencia) {
        this.tabelasPreJob.putAll(evidencia);
        return this;
    }
    
    public ItemPilha addTabelasPosJob(String tabela, EvidenciaTabela evidencia) {
        this.tabelasPosJob.put(tabela, evidencia);
        return this;
    }
    
    public ItemPilha addTabelasPosJob(Map<String, EvidenciaTabela> evidencia) {
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
