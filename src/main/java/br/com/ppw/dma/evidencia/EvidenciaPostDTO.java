package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.job.AgendaID;
import br.com.ppw.dma.job.ComandoSql;
import br.com.ppw.dma.master.MasterDtoRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EvidenciaPostDTO implements MasterDtoRequest {

    Integer ordem;
    AgendaID id;
    String job;
    List<String> argumentos = new ArrayList<>();
    List<ComandoSql> queries = new ArrayList<>();
    List<String> cargas = new ArrayList<>();
    //List<ExtrcaoBanco> tabelasPreJob = new ArrayList<>();
    //List<ExtrcaoBanco> tabelasPosJob = new ArrayList<>();
    List<File> logs = new ArrayList<>();
    List<File> entradas = new ArrayList<>();
    List<File> saidas = new ArrayList<>();
    boolean sucesso = false;

//    public EvidenciaPostDTO addTabelasPreJob(ExtrcaoBanco extrcaoBanco) {
//        this.tabelasPreJob.add(extrcaoBanco);
//        return this;
//    }

//    public EvidenciaPostDTO addTabelasPreJob(List<ExtrcaoBanco> extrcaoBanco) {
//        this.tabelasPreJob.addAll(extrcaoBanco);
//        return this;
//    }

//    public EvidenciaPostDTO addTabelasPosJob(ExtrcaoBanco extrcaoBanco) {
//        this.tabelasPosJob.add(extrcaoBanco);
//        return this;
//    }

//    public EvidenciaPostDTO addTabelasPosJob(List<ExtrcaoBanco> extrcaoBanco) {
//        this.tabelasPosJob.addAll(extrcaoBanco);
//        return this;
//    }

    public EvidenciaPostDTO addArgumentos(String arg) {
        argumentos.add(arg);
        return this;
    }

    public EvidenciaPostDTO addArgumentos(List<String> args) {
        argumentos.addAll(args);
        return this;
    }

    public EvidenciaPostDTO limparArgumentos() {
        argumentos.clear();
        return this;
    }

    public EvidenciaPostDTO addEvidencias(File arquivo) {
        logs.add(arquivo);
        return this;
    }

    public EvidenciaPostDTO addEvidencias(List<File> arquivo) {
        logs.addAll(arquivo);
        return this;
    }

    public EvidenciaPostDTO limparEvidencias() {
        logs.clear();
        return this;
    }

    public EvidenciaPostDTO addEntradas(File arquivo) {
        entradas.add(arquivo);
        return this;
    }

    public EvidenciaPostDTO addEntradas(List<File> arquivo) {
        entradas.addAll(arquivo);
        return this;
    }

    public EvidenciaPostDTO limparEntradas() {
        entradas.clear();
        return this;
    }

    public EvidenciaPostDTO addSaidas(File arquivo) {
        saidas.add(arquivo);
        return this;
    }

    public EvidenciaPostDTO addSaidas(List<File> arquivo) {
        saidas.addAll(arquivo);
        return this;
    }

    public EvidenciaPostDTO limparSaidas() {
        saidas.clear();
        return this;
    }
}
