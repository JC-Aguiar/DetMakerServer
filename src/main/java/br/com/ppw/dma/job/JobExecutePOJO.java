package br.com.ppw.dma.job;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ResultadoSql;
import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.net.RemoteFile;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecutePOJO {

    AmbienteAcessoDTO banco;
    Job job;
    JobInfoDTO jobInfo;
    Integer ordem;
    String argumentos;
    List<ResultadoSql> tabelas = new ArrayList<>();
    List<String> terminal = new ArrayList<>();
    List<RemoteFile> logs = new ArrayList<>();
    List<RemoteFile> cargas = new ArrayList<>();
    List<RemoteFile> saidas = new ArrayList<>();
    boolean sucesso = false;
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    //TODO: precisa da informação da pipeline
    //TODO: adicionar método toString() manual para evitar de exibir conteúdos massivos


    public JobExecutePOJO(
        @NonNull Job job,
        @NonNull JobExecuteDTO dto,
        @NonNull AmbienteAcessoDTO banco) {
        //----------------------------------------
        setJob(job);
        setJobInfo(JobInfoDTO.converterJob(job));
        setOrdem(dto.getOrdem());
        setArgumentos(dto.getArgumentos());
        addComandoSql(dto.getQueries());
        setBanco(banco);
        //TODO: add cargas!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public JobExecutePOJO(@NonNull Evidencia evidencia) {
        setJob(evidencia.getJob());
        setJobInfo(JobInfoDTO.converterJob(job));
        setOrdem(evidencia.getOrdem());
        setArgumentos(evidencia.getArgumentos());
        val comandos = evidencia.getBanco()
            .stream()
            .map(ComandoSql::new)
            .toList();
        addComandoSql(comandos);
        //TODO: add cargas!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public String getTerminalFormatado() {
        return String.join("\n", terminal).trim();
    }

    public List<ResultadoSql> getTabelas() {
        return tabelas.stream()
            .filter(tabela -> !tabela.getSqlCompleta().trim().isEmpty())
            .toList();
    }

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

    public JobExecutePOJO addLogs(@NonNull RemoteFile arquivo) {
        logs.add(arquivo);
        return this;
    }

    public JobExecutePOJO addLogs(@NonNull List<RemoteFile> arquivo) {
        logs.addAll(arquivo);
        return this;
    }

    public JobExecutePOJO addCargas(@NonNull RemoteFile arquivo) {
        cargas.add(arquivo);
        return this;
    }

    public JobExecutePOJO addCargas(@NonNull List<RemoteFile> arquivo) {
        cargas.addAll(arquivo);
        return this;
    }

    public JobExecutePOJO addProdutos(@NonNull RemoteFile arquivo) {
        saidas.add(arquivo);
        return this;
    }

    public JobExecutePOJO addProdutos(@NonNull List<RemoteFile> arquivo) {
        saidas.addAll(arquivo);
        return this;
    }

    public String comandoShell() {
        return "ksh " + jobInfo.pathShell() + " " + argumentos;
    }
}
