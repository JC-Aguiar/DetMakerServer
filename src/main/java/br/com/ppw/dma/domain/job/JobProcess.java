package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.jobQuery.ResultadoSql;
import br.com.ppw.dma.net.RemoteFile;
import br.com.ppw.dma.net.SftpFileManager;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobProcess {

    Job job;
    JobInfoDTO jobInfo;
    JobExecuteDTO jobInputs;
    AmbienteAcessoDTO banco;
    String sha256;
    List<String> terminal = new ArrayList<>();
    final List<ResultadoSql> tabelasPreJob = new ArrayList<>();
    final List<ResultadoSql> tabelasPosJob = new ArrayList<>();
    final List<SftpFileManager<RemoteFile>> logs = new ArrayList<>();
    final List<SftpFileManager<File>> cargas = new ArrayList<>();
    final List<SftpFileManager<RemoteFile>> saidas = new ArrayList<>();
    boolean sucesso = false;
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    Integer exitCode;
    String erroFatal;
    //TODO: precisa da informação da pipeline?
    //TODO: adicionar método toString() manual para evitar de exibir conteúdos massivos

//    static final String TEMPO_AUSENTE = "Indisponível data de início/fim da execução";
//    static final String TEMPO_TOTAL = " O Job rodou em %d segundos";
//    static final String EXIT_CODE_AUSENTE = "Aplicação não obteve código de retorno do Job";
//    static final String SQL_DIFF_ZERO = "   * '%s': esta query não gerou impacto no banco";
//    static final String SQL_DIFF_TOTAL = "   * '%s': esta query impactou %d registro(s)";
//    static final String LOG_TERMINAL_AUSENTE = " - O terminal não exibiu nenhuma informação do processo";
//    static final String ANALISE_TEMPLATE = """
//        DADOS DA EXECUÇÃO
//         - Sha256: '%s'
//         - Duração: %s
//         - Exit-code: %s
//         - Status da execução: %s
//        INCONFORMIDADES
//         - Total de inconformidades: %d
//         %s
//        TABELAS IMPACTADAS
//         - Total de tabelas informadas na Schedule: %d
//         - Total de consultas realizadas: %d
//         - Tabelas não consultadas: %s
//         - Tabelas consultadas em adicional: %s
//         %s
//        LOGS CAPTURADOS
//         %s
//         - Total de máscaras de logs na Schedule: %d
//         - Total de arquivos de logs obtidos: %d
//        CARGAS ENVIADAS
//         - Total de máscaras de cargas na Schedule: %d
//         - Total de arquivos de cargas enviados: %d
//        PRODUTOS GERADOS
//         - Total de máscaras de produtos na Schedule: %d
//         - Total de arquivos produzidos: %d
//        """;

    public JobProcess(@NonNull JobPreparation dados) {
        setJob(dados.job());
        setJobInfo(JobInfoDTO.converterJob(dados.job()));
        setJobInputs(dados.jobInputs());
        setDataInicio(OffsetDateTime.now());
    }

    public String comandoShell() {
        return "ksh "
            + jobInfo.pathShell()
            + " "
            + jobInputs.getArgumentos();
    }

    public String getTerminalFormatado() {
        return String.join("\n", terminal).trim();
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
        logs.add(arquivo);
    }

    //    public JobProcess addLogsPreJob(@NonNull String referencia, @NonNull SftpFileManager<RemoteFile> arquivo) {
//        logsPreJob.put(referencia, arquivo);
//        return this;
//    }
//
//    public JobProcess addLogsPosJob(@NonNull String referencia, @NonNull SftpFileManager<RemoteFile> arquivo) {
//        logsPosJob.put(referencia, arquivo);
//        return this;
//    }

    public void addCargas(@NonNull SftpFileManager<File> arquivo) {
        cargas.add(arquivo);
    }

    public void addCargas(@NonNull List<SftpFileManager<File>> arquivo) {
        cargas.addAll(arquivo);
    }

    public void addProdutos(@NonNull SftpFileManager<RemoteFile> arquivo) {
        saidas.add(arquivo);
    }

    public void addProdutos(@NonNull List<SftpFileManager<RemoteFile>> arquivo) {
        saidas.addAll(arquivo);
    }

    public boolean possuiInicioFim() {
        return dataInicio != null && dataFim != null;
    }

    public long getDuracao() {
        if(!possuiInicioFim()) return -1;
        return Duration.between(dataInicio.toInstant(), dataFim.toInstant()).getSeconds();
    }

//    public void analisarExecucao() {
//        //Anexando arquivos de log pós-execução e obtendo avisos de inconformidades
//        logsPreJob.forEach((ref, log) -> {
//            logsPosJob
//        });
//        //Anexando arquivos de saída e avisos de inconformidades
//        for(val gerenciador : saidas) {
//            gerenciador.getFile().ifPresent(dados::addProdutos);
//            if(gerenciador.getStatus().error)
//                dados.getErros().add(gerenciador.getMensagemStatus());
//        }
//
//        val status = (exitCode != null && exitCode == SUCCESS.code) ?
//            "Sucesso" : "Falha";
//
//        val errosMensagem = erroFatal.stream()
//            .map(exception -> "   - "+exception)
//            .collect(Collectors.joining("\n"));
//
//        val tabelasConsultadas = tabelas.stream()
//            .map(ResultadoSql::getTabela)
//            .toList();
//
//        val tabelasPendentes = new ArrayList<>(job.getTabelas());
//        tabelasPendentes.removeAll(tabelasConsultadas);
//
//        val tabelasExtras = new ArrayList<>(tabelasConsultadas);
//        tabelasPendentes.removeAll(job.getTabelas());
//
//        val infoTabelas = new StringBuilder();
//        for (val table : tabelas) {
//            val diff = table.getResultado();
//            diff.removeAll(table.getResultadoPreJob());
//            if (diff.isEmpty())
//                infoTabelas.append(String.format(SQL_DIFF_ZERO, table));
//            else
//                infoTabelas.append(String.format(SQL_DIFF_TOTAL, table, diff.size()));
//        }
//
//         analise = String.format(ANALISE_TEMPLATE,
//             sha256,
//             !possuiInicioFim() ? TEMPO_AUSENTE : String.format(TEMPO_TOTAL, getDuracao()),
//             exitCode == null ? EXIT_CODE_AUSENTE : exitCode,
//             status,
//             erroFatal.size(),
//             erroFatal.isEmpty() ? "" : errosMensagem,
//             job.getTabelas().size(),
//             tabelasConsultadas.size(),
//             tabelasPendentes.isEmpty() ? "0" : String.join("\n", tabelasPendentes),
//             tabelasExtras.isEmpty() ? "0" : String.join("\n", tabelasExtras),
//             infoTabelas,
//             terminal.isEmpty() ? LOG_TERMINAL_AUSENTE : "",
//             job.getMascaraLog().size(),
//             logs.size(),
//             job.getMascaraEntrada().size(),
//             cargas.size(),
//             job.getMascaraSaida().size(),
//             saidas.size()
//         );
//    }

}
