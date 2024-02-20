package br.com.ppw.dma.job;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ResultadoSql;
import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.net.RemoteFile;
import br.com.ppw.dma.system.Arquivos;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static br.com.ppw.dma.system.ExitCodes.SUCCESS;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecutePOJO {

    AmbienteAcessoDTO banco;
    Job job;
    JobInfoDTO jobInfo;
    String sha256;
    Integer ordem;
    String argumentos;
    List<ResultadoSql> tabelas = new ArrayList<>();
    List<String> terminal = new ArrayList<>();
    List<RemoteFile> logs = new ArrayList<>();
    List<File> cargas = new ArrayList<>();
    List<RemoteFile> saidas = new ArrayList<>();
    boolean sucesso = false;
    OffsetDateTime dataInicio;
    OffsetDateTime dataFim;
    Integer exitCode;
    final List<String> erros = new ArrayList<>();
    String analise;
    //TODO: precisa da informação da pipeline?
    //TODO: adicionar método toString() manual para evitar de exibir conteúdos massivos

    static final String EXEC_TITULO = "DADOS DA EXECUÇÃO";
    static final String EXEC_SHA = " - Sha256: '%s'.";
    static final String EXEC_TEMPO_AUSENTE = " - O teste não conseguiu marcar data-hora de início ou fim da execução.";
    static final String EXEC_TEMPO_TOTAL = " - O Job durou %d segundos.";
    static final String EXEC_RETORNO_COD_AUSENTE = " - O teste não conseguiu obter nenhum código de retorno.";
    static final String EXEC_RETORNO_COD_FINAL = " - Código de retorno: %d.";
    static final String EXEC_STATUS = " - Status técnico da execução: %s.";
    static final String ERROS_TITULO = "INCONFORMIDADES";
    static final String ERROS_AUSENTES = " - Nenhuma inconformidade ocorreu durante a execução.";
    static final String ERROS_TOTAIS = " - Total de inconformidades: %d.";
    static final String DB_TITULO = "TABELAS IMPACTADAS";
    static final String DB_SCHEDULE_AUSENTE = " - Nenhuma tabela informada na Schedule.";
    static final String DB_SCHEDULE_TOTAL = " - Total de tabelas informadas na Schedule: %d.";
    static final String DB_TABELAS_AUSENTES = " - Nenhuma tabela foi consultada antes ou depois da execução.";
    static final String DB_TABELAS_TOTAIS = " - Total de consultas realizadas: %d.";
    static final String DB_TABELAS_PENDENTES = " - Tabelas não consultadas: %s.";
    static final String DB_TABELAS_EXTRAS = " - Tabelas consultadas em adicional: %s.";
    static final String DB_TABELA_DIFF_ZERO = " - Tabela '%s' não foi alterada pela execução deste teste.";
    static final String DB_TABELA_DIFF_TOTAL = " - Tabela '%s' teve %d registro(s) impactado(s) pelo teste.";
    static final String LOG_TITULO = "LOGS CAPTURADOS";
    static final String LOG_TERMINAL_AUSENTE = " - O terminal não exibiu nenhuma informação do que foi processado.";
    static final String LOG_SCHEDULE_AUSENTE = " - Nenhuma máscara de log informada na Schedule.";
    static final String LOG_SCHEDULE_TOTAL = " - Total de máscaras de log na Schedule: %d.";
    static final String LOG_ARQUIVOS_AUSENTES = " - Nenhum log foi obtido.";
    static final String LOG_ARQUIVOS_TOTAIS = " - Total de logs obtidos: %d.";
    static final String CARGAS_TITULO = "CARGAS ENVIADAS";
    static final String CARGAS_SCHEDULE_AUSENTE = " - Nenhuma máscara de carga informada na Schedule.";
    static final String CARGAS_SCHEDULE_TOTAL = " - Total de máscaras de carga na Schedule: %d.";
    static final String CARGAS_ARQUIVOS_AUSENTES = " - Nenhuma carga foi enviada.";
    static final String CARGAS_ARQUIVOS_TOTAIS = " - Total de cargas enviadas: %d.";
    static final String DADOS_TITULO = "PRODUTOS GERADOS";
    static final String DADOS_SCHEDULE_AUSENTE = " - Nenhuma máscara de produto informada na Schedule.";
    static final String DADOS_SCHEDULE_TOTAL = " - Total de máscaras de produto na Schedule: %d.";
    static final String DADOS_ARQUIVOS_AUSENTES = " - Nenhum produto foi obtido.";
    static final String DADOS_ARQUIVOS_TOTAIS = " - Total de produtos obtidos: %d.";


    //Construção pra cenários de nova execução
    public JobExecutePOJO(
        @NonNull Job job,
        @NonNull JobExecuteDTO dto,
        @NonNull AmbienteAcessoDTO banco,
        @NonNull List<File> cargas) {
        //----------------------------------------
        setJob(job);
        setJobInfo(JobInfoDTO.converterJob(job));
        setOrdem(dto.getOrdem());
        setArgumentos(dto.getArgumentos());
        addComandoSql(dto.getQueries());
        setBanco(banco);
//        setCargas(dto.getCargas());
        setCargas(cargas);
    }

    //Construção para cenários de reexecução
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

    public JobExecutePOJO addCargas(@NonNull File arquivo) {
        cargas.add(arquivo);
        return this;
    }

    public JobExecutePOJO addCargas(@NonNull List<File> arquivo) {
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

    public boolean possuiInicioFim() {
        return dataInicio != null && dataFim != null;
    }

    public long getDuracao() {
        if(!possuiInicioFim()) return -1;
        return Duration.between(dataInicio.toInstant(), dataFim.toInstant()).getSeconds();
    }

    public boolean possuiExitCode() {
        return exitCode == null;
    }


    public void analisarExecucao() {
        val status = (exitCode != null && exitCode == SUCCESS.code) ? "Sucesso" : "Falha";

        val infoDuracao = !possuiInicioFim() ?
            EXEC_TEMPO_AUSENTE :
            String.format(EXEC_TEMPO_TOTAL, getDuracao());

        val infoExitCode = !possuiExitCode() ?
            EXEC_RETORNO_COD_AUSENTE :
            String.format(EXEC_RETORNO_COD_FINAL, exitCode);

        val infoErrosMensagem = erros.stream()
            .map(erro -> "   - "+erro)
            .collect(Collectors.joining("\n"));

        val infoErros = erros.isEmpty() ?
            ERROS_AUSENTES :
            String.format(ERROS_TOTAIS, erros.size()) + "\n" + infoErrosMensagem;

        val infoTabelas = new StringBuilder();

        val tabelasConsultadas = tabelas.stream()
            .map(ResultadoSql::getTabela)
            .toList();

        val tabelasPendentes = new ArrayList<>(jobInfo.getTabelas());
        tabelasPendentes.removeAll(tabelasConsultadas);

        val tabelasExtras = new ArrayList<>(tabelasConsultadas);
        tabelasPendentes.removeAll(jobInfo.getTabelas());

        if(jobInfo.getTabelas().isEmpty())
            infoTabelas.append(DB_SCHEDULE_AUSENTE);
        else {
            infoTabelas.append(String.format(
                DB_SCHEDULE_TOTAL, jobInfo.getTabelas().size()
            ));
        }
        if(tabelas.isEmpty())
            infoTabelas.append(DB_TABELAS_AUSENTES);
        else {
            infoTabelas.append(
                String.format(DB_TABELAS_TOTAIS, tabelas.size())
            );
        }
        if(!tabelasPendentes.isEmpty()) {
            infoTabelas.append(String.format(
                DB_TABELAS_PENDENTES, String.join(", ", tabelasPendentes)
            ));
        }
        if(!tabelasExtras.isEmpty()) {
            infoTabelas.append(String.format(
                DB_TABELAS_EXTRAS, String.join(", ", tabelasExtras)
            ));
        }



        return String.join("\n",
            //Dados gerais
            EXEC_TITULO,
            String.format(EXEC_SHA, sha256),
            infoDuracao,
            infoExitCode,
            String.format(EXEC_STATUS, status),
            //Mensagem de erro
            ERROS_TITULO,
            infoErros,
            //Tabelas da schedule e tabelas consultadas
            DB_TITULO,
            infoTabelas.toString(),

            );
        }
        comentario.append("TABELAS IMPACTADAS\n");
        else {
            if(!tabelasPendentes.isEmpty()) {
                comentario.append(" - Tabelas não consultadas: ")
                    .append(String.join(", ", tabelasPendentes))
                    .append(".\n");
            }
            if(!tabelasExtras.isEmpty()) {
                comentario.append(" - Tabelas consultadas em adicional: ")
                    .append(String.join(", ", tabelasExtras))
                    .append(".\n");
            }
            for(val tabela : tabelas) {
                val diff = tabela.getResultadoPosJob();
                diff.removeAll(tabela.getResultadoPreJob());
                comentario.append(" - Tabela '" +tabela+ "' ");
                if(diff.isEmpty()) comentario.append("não foi alterada pela execução deste teste.\n");
                else comentario.append("teve " +diff.size()+ " registros impactados pelo teste.\n");
            }
        }
        //Logs e terminal
        comentario.append("LOGS\n");
        if(terminal.isEmpty())
            comentario.append(" - O terminal não exibiu nenhuma informação do que foi processado.\n");
        if(jobInfo.getMascaraLog().isEmpty())
            comentario.append(" - Nenhuma máscara de log informada na Schedule.\n");
        else
            comentario.append(" - Total de máscaras de log na Schedule: " +jobInfo.getMascaraLog().size()+ ".\n");
        if(logs.isEmpty())
            comentario.append(" - Nenhum log foi obtido.\n");
        else
            comentario.append(" - Total de logs obtidos: " +logs.size()+ ".\n");

        //Arquivos carregados
        comentario.append("CARGAS\n");
        if(jobInfo.getMascaraEntrada().isEmpty())
            comentario.append(" - Nenhuma máscara de carga informada na Schedule.\n");
        else
            comentario.append(" - Total de máscaras de carga na Schedule: " +jobInfo.getMascaraEntrada().size()+ ".\n");
        if(cargas.isEmpty())
            comentario.append(" - Nenhuma carga foi obtida.\n");
        else
            comentario.append(" - Total de cargas obtidas: " +cargas.size()+ ".\n");

        //Arquivos gerados
        comentario.append("PRODUTOS\n");
        if(jobInfo.getMascaraSaida().isEmpty())
            comentario.append(" - Nenhuma máscara de produto informada na Schedule.\n");
        else
            comentario.append(" - Total de máscaras de produto na Schedule: " +jobInfo.getMascaraSaida().size()+ ".\n");
        if(saidas.isEmpty())
            comentario.append(" - Nenhum produto foi obtido.\n");
        else
            comentario.append(" - Total de produtos obtidos: " +saidas.size()+ ".\n");

        //Fim
        analise = comentario.toString();
    }
}
