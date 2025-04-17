package br.com.ppw.dma.domain.task.result;

import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.jobQuery.ResultadoSql;
import br.com.ppw.dma.domain.task.TaskPayloadJob;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.net.RemoteFile;
import br.com.ppw.dma.net.SftpFileManager;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobProcess extends TaskPayloadJob {


    //TODO: adicionar validação de campos obrigatórios.

//    Job job;
    String ticket;
    AmbienteAcessoDTO banco;
    ConectorSftp sftp;
    String versao;
    //@ToString.Exclude
    //SftpTerminalManager terminal;
    final List<ResultadoSql> tabelasPreJob = new ArrayList<>();
    final List<ResultadoSql> tabelasPosJob = new ArrayList<>();
    final List<SftpFileManager<File>> cargasEnviadas = new ArrayList<>();
    final List<SftpFileManager<RemoteFile>> cargasColetadas = new ArrayList<>();
    final List<SftpFileManager<RemoteFile>> logsColetados = new ArrayList<>();
    final List<SftpFileManager<RemoteFile>> remessasColetadas = new ArrayList<>();
    boolean sucesso = false;
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    Integer exitCode;
    String erroFatal;


    public JobProcess(@NonNull TaskPayloadJob dados, @NonNull AmbienteAcessoDTO banco, @NonNull ConectorSftp sftp) {
        setBanco(banco);
        setSftp(sftp);
        setNome(dados.getNome());
        setDescricao(dados.getDescricao());
        setOrdem(dados.getOrdem());
        setComandoExec(dados.getComandoExec());
        setComandoVersao(dados.getComandoVersao());
        setQueriesExec(dados.getQueriesExec());
        setCargasEnvio(dados.getCargasEnvio());
        setCargasMascara(dados.getCargasMascara());
        setLogsMascara(dados.getLogsMascara());
        setRemessasMascara(dados.getRemessasMascara());
        setDirCargaEnvio(dados.getDirCargaEnvio());
        this.dataInicio = OffsetDateTime.now();
    }

    public boolean possuiTabelas() {
        return tabelasPreJob.size() + tabelasPosJob.size() > 0;
    }

    public void addTabelasPreJob(@NonNull ResultadoSql evidencia) {
        if(!evidencia.getQuery().trim().isEmpty()) {
            this.tabelasPreJob.add(evidencia);
        }
    }

    public void addTabelasPreJob(@NonNull List<ResultadoSql> evidencia) {
        evidencia.forEach(this::addTabelasPreJob);
    }

    public void addTabelasPosJob(@NonNull ResultadoSql evidencia) {
        if(!evidencia.getQuery().trim().isEmpty()) {
            this.tabelasPosJob.add(evidencia);
        }
    }

    public void addTabelasPosJob(@NonNull List<ResultadoSql> evidencia) {
        evidencia.forEach(this::addTabelasPosJob);
    }

    public void addLogs(@NonNull SftpFileManager<RemoteFile> arquivo) {
        logsColetados.add(arquivo);
    }

    public void addLogs(@NonNull List<SftpFileManager<RemoteFile>> arquivo) {
        logsColetados.addAll(arquivo);
    }

    public void addCargas(@NonNull SftpFileManager<RemoteFile> arquivo) {
        cargasColetadas.add(arquivo);
    }

    public void addCargas(@NonNull List<SftpFileManager<RemoteFile>> arquivo) {
        cargasColetadas.addAll(arquivo);
    }

    public void addRemessas(@NonNull SftpFileManager<RemoteFile> arquivo) {
        remessasColetadas.add(arquivo);
    }

    public void addRemessas(@NonNull List<SftpFileManager<RemoteFile>> arquivo) {
        remessasColetadas.addAll(arquivo);
    }

    public boolean possuiInicioFim() {
        return dataInicio != null && dataFim != null;
    }

    public long getDuracao() {
        if(!possuiInicioFim()) return -1;
        return Duration.between(dataInicio.toInstant(), dataFim.toInstant()).getSeconds();
    }

    public String getContexto() {
        return String.format("%dº Job ['%s']", getOrdem()+1, getNome());
    }

}
