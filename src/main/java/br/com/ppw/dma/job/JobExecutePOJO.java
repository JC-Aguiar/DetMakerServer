package br.com.ppw.dma.job;

import br.com.ppw.dma.master.MasterResponseDTO;
import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.ResultadoSql;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecutePOJO implements MasterResponseDTO {

    Job job;
    JobInfoDTO jobInfo;
    Integer ordem;
    List<String> argumentos = new ArrayList<>();
    List<ResultadoSql> tabelas = new ArrayList<>();
    List<File> logs = new ArrayList<>();
    List<File> cargas = new ArrayList<>();
    List<File> produtos = new ArrayList<>();
    boolean sucesso = false;


    public JobExecutePOJO addComandoSql(@NonNull ComandoSql comandoSql) {
        this.tabelas.add(new ResultadoSql(comandoSql));
        return this;
    }

    public JobExecutePOJO addComandoSql(@NonNull List<ComandoSql> comandosSql) {
        comandosSql.forEach(cmdSql -> this.tabelas.add(new ResultadoSql(cmdSql)));
        return this;
    }
    
    public JobExecutePOJO addResultado(@NonNull ResultadoSql evidencia) {
        this.tabelas.add(evidencia);
        return this;
    }

    public JobExecutePOJO addResultado(@NonNull List<ResultadoSql> evidencia) {
        this.tabelas.addAll(evidencia);
        return this;
    }

    public JobExecutePOJO addArgumentos(String arg) {
        argumentos.add(arg);
        return this;
    }

    public JobExecutePOJO addArgumentos(@NonNull List<String> args) {
        argumentos.addAll(args);
        return this;
    }

    public JobExecutePOJO limparArgumentos() {
        argumentos.clear();
        return this;
    }

    public JobExecutePOJO addEvidencias(@NonNull File arquivo) {
        logs.add(arquivo);
        return this;
    }

    public JobExecutePOJO addEvidencias(@NonNull List<File> arquivo) {
        logs.addAll(arquivo);
        return this;
    }

    public JobExecutePOJO limparEvidencias() {
        logs.clear();
        return this;
    }

    public JobExecutePOJO addCargas(@NonNull File arquivo) {
        cargas.add(arquivo);
        return this;
    }

    public JobExecutePOJO addCargas(@NonNull List<File> arquivo) {
        cargas.addAll(arquivo);
        return this;
    }

    public JobExecutePOJO limparCargas() {
        cargas.clear();
        return this;
    }

    public JobExecutePOJO addProdutos(@NonNull File arquivo) {
        produtos.add(arquivo);
        return this;
    }

    public JobExecutePOJO addProdutos(@NonNull List<File> arquivo) {
        produtos.addAll(arquivo);
        return this;
    }

    public JobExecutePOJO limparProdutos() {
        produtos.clear();
        return this;
    }

    public String getParametro() {
        return String.join(" ", argumentos);
    }

    public String getQuer() {
        return String.join(" ", argumentos);
    }

    public String comandoShell() {
        return "ksh "
            .concat(jobInfo.pathShell())
            .concat(" ")
            .concat(getParametro());
    }

    public String pathLog() {
        return "ksh "
            .concat(jobInfo.pathShell())
            .concat(" ")
            .concat(getParametro());
    }
}
