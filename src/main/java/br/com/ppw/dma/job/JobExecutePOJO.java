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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecutePOJO implements MasterResponseDTO {

    Job job;
    JobInfoDTO jobInfo;
    Integer ordem;
    String argumentos;
    List<ResultadoSql> tabelas = new ArrayList<>();
    List<File> logs = new ArrayList<>();
    List<File> cargas = new ArrayList<>();
    List<File> saidas = new ArrayList<>();
    boolean sucesso = false;
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    //TODO: precisa da informação da pipeline

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

    public JobExecutePOJO addLogs(@NonNull File arquivo) {
        logs.add(arquivo);
        return this;
    }

    public JobExecutePOJO addLogs(@NonNull List<File> arquivo) {
        logs.addAll(arquivo);
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

    public JobExecutePOJO addProdutos(@NonNull File arquivo) {
        saidas.add(arquivo);
        return this;
    }

    public JobExecutePOJO addProdutos(@NonNull List<File> arquivo) {
        saidas.addAll(arquivo);
        return this;
    }

    public String comandoShell() {
        return "ksh " + jobInfo.pathShell() + " " + argumentos;
    }
}
