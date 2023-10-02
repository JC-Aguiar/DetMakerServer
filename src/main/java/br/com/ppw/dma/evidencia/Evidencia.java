package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.job.AgendaDTO;
import br.com.ppw.dma.job.ComandoSql;
import br.com.ppw.dma.master.MasterEntity;
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
//@Entity(name = "evidencias")
//@Table(name = "evidencias")
public class Evidencia implements MasterEntity<Long> {

    /*
    Long id
    Integer ordem
    Job job
    String argumentos
    Set<ExecFile> cargas
    Set<ExecQuery> bancoPreJob
    Set<ExecQuery> bancoPosJob
    Set<ExecFile> logs
    Set<ExecFile> saidas
    Boolean sucesso
    OffsetDateTime dataExecInicio
    OffsetDateTime dataExecFim
     */

//    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Integer ordem;
    AgendaDTO registro;
    List<String> argumentos = new ArrayList<>();
    List<ComandoSql> queries = new ArrayList<>();
    List<String> cargas = new ArrayList<>();
    Map<ComandoSql, ExtrcaoBanco> tabelasPreJob = new HashMap<>();
    Map<ComandoSql, ExtrcaoBanco> tabelasPosJob = new HashMap<>();
    List<File> logs = new ArrayList<>();
    List<File> entradas = new ArrayList<>();
    List<File> saidas = new ArrayList<>();
    boolean sucesso = false;

    public Evidencia addTabelasPreJob(ComandoSql comandoSql, ExtrcaoBanco evidencia) {
        this.tabelasPreJob.put(comandoSql, evidencia);
        return this;
    }

    public Evidencia addTabelasPreJob(Map<ComandoSql, ExtrcaoBanco> evidencia) {
        this.tabelasPreJob.putAll(evidencia);
        return this;
    }

    public Evidencia addTabelasPosJob(ComandoSql comandoSql, ExtrcaoBanco evidencia) {
        this.tabelasPosJob.put(comandoSql, evidencia);
        return this;
    }

    public Evidencia addTabelasPosJob(Map<ComandoSql, ExtrcaoBanco> evidencia) {
        this.tabelasPosJob.putAll(evidencia);
        return this;
    }

    public Evidencia addArgumentos(String arg) {
        argumentos.add(arg);
        return this;
    }

    public Evidencia addArgumentos(List<String> args) {
        argumentos.addAll(args);
        return this;
    }

    public Evidencia limparArgumentos() {
        argumentos.clear();
        return this;
    }

    public Evidencia addEvidencias(File arquivo) {
        logs.add(arquivo);
        return this;
    }

    public Evidencia addEvidencias(List<File> arquivo) {
        logs.addAll(arquivo);
        return this;
    }

    public Evidencia limparEvidencias() {
        logs.clear();
        return this;
    }

    public Evidencia addEntradas(File arquivo) {
        entradas.add(arquivo);
        return this;
    }

    public Evidencia addEntradas(List<File> arquivo) {
        entradas.addAll(arquivo);
        return this;
    }

    public Evidencia limparEntradas() {
        entradas.clear();
        return this;
    }

    public Evidencia addSaidas(File arquivo) {
        saidas.add(arquivo);
        return this;
    }

    public Evidencia addSaidas(List<File> arquivo) {
        saidas.addAll(arquivo);
        return this;
    }

    public Evidencia limparSaidas() {
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
