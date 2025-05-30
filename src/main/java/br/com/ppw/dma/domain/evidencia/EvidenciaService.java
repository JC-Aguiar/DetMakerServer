package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.execFile.ExecFileService;
import br.com.ppw.dma.domain.execQuery.ExecQuery;
import br.com.ppw.dma.domain.execQuery.ExecQueryService;
import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.task.result.EvidenciaResult;
import br.com.ppw.dma.domain.task.result.JobProcess;
import br.com.ppw.dma.domain.task.result.PipelineResult;
import br.com.ppw.dma.domain.relatorio.Relatorio;
import br.com.ppw.dma.domain.relatorio.RelatorioService;
import br.com.ppw.dma.domain.relatorio.TiposDeTeste;
import com.google.gson.Gson;
import jakarta.persistence.PersistenceException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Service
@Slf4j
public class EvidenciaService extends MasterService<Long, Evidencia, EvidenciaService> {

    private final EvidenciaRepository evidenciaDao;
    private final ExecFileService execFileService;
    private final ExecQueryService execQueryService;
    private final RelatorioService relatorioService;
    private final Gson gson;

    @Autowired
    public EvidenciaService(
        EvidenciaRepository evidenciaDao,
        ExecFileService execFileService,
        ExecQueryService execQueryService,
        RelatorioService relatorioService,
        Gson gson) {
        //---------------------------------------
        super(evidenciaDao);
        this.evidenciaDao = evidenciaDao;
        this.execFileService = execFileService;
        this.execQueryService = execQueryService;
        this.relatorioService = relatorioService;
        this.gson = gson;
    }


    //TODO: javadoc
//    @Transactional(noRollbackFor = Throwable.class)
    public PipelineResult gerarEvidencia(@NonNull PipelineResult pipelineResult) {
        var jobsResult = pipelineResult.getResultadoJobs();

        log.info("Criando novo Relatório para novas Evidências a se executar.");
        var dataHora = OffsetDateTime.now(RELOGIO);
        var relatorio = Relatorio.builder()
            .cliente(pipelineResult.getClienteNome())
            .ambiente(pipelineResult.getAmbiente())
            .pipelineNome(pipelineResult.getPipelineNome())
            .pipelineDescricao(pipelineResult.getPipelineDescricao())
//            .inconformidades(evidenciaErros.toString())
            .ticket(pipelineResult.getTicket())
            .usuario(pipelineResult.getUsuario())
            .dataCompleta(dataHora)
            .data(dataHora.toLocalDate())
            .testeTipo(TiposDeTeste.UNITARIO)
            .sucesso(!pipelineResult.isErro())
            .erroFatal(pipelineResult.getMensagemErro())
            .build();
//        relatorioService.save(relatorio);

        log.info("Iniciando geração de Evidências para {} registro(s).", jobsResult.size());
        var evidencias = jobsResult.stream()
            .map(job -> gerarEvidencia(relatorio, job))
            .toList();
        pipelineResult.addEvidenciaResult(evidencias);

        relatorioService.save(relatorio);
        return pipelineResult;
    }

    //TODO: javadoc
//    @Transactional(noRollbackFor = Throwable.class)
    public EvidenciaResult gerarEvidencia(@NonNull Relatorio relatorio, @NonNull JobProcess process) {
        log.info("Gerando Evidência para {}.", process.getContexto());
        Function<String, String> criarMensagemErro = (erro) -> String.format(
            "Erro na criação da Evidência para o %s: %s",
            process.getContexto(),
            erro);

        try {
            var evidencia = new Evidencia(process);
            evidencia.setRelatorio(relatorio);
            log.info(evidencia.toString());

            //TODO: Precisa melhorar esse processo. Criar uma tabela relacionada só de erros
            //      e tratar cada erro de cada tentativa de isnert individualmente,
            //      também para não sobrecarregar uam coluna do banco com N possíveis erros.
            saveExecQueries(process, evidencia);
            saveExecCargas(process, evidencia);
            saveExecLogs(process, evidencia);
            builExecRemessas(process, evidencia);

            if(evidencia.getMensagemErro() == null || evidencia.getMensagemErro().isEmpty()) {
                save(evidencia);
                return EvidenciaResult.ok(evidencia);  //TODO: melhorar
            }
            evidencia.setRevisor("Det-Maker");
            evidencia.setDataRevisao(OffsetDateTime.now(RELOGIO));
            evidencia.setStatus(TipoEvidenciaStatus.REPROVADO);
            evidencia.setComentario("O Job obteve algum tipo de erro " +
                "e seu resultado foi definido automaticamente para " +
                TipoEvidenciaStatus.REPROVADO.status
            );
            save(evidencia);
            return EvidenciaResult.ok(evidencia);  //TODO: melhorar
        }
        catch(PersistenceException e) {
            var mensagem = criarMensagemErro.apply(SqlSintaxe.getExceptionMainCause(e));
            log.error(mensagem);
            Optional.ofNullable(relatorio.getInconformidades())
                .map(inconf -> inconf + ". " + mensagem)
                .ifPresentOrElse(
                    relatorio::setInconformidades,
                    () -> relatorio.setInconformidades(mensagem)
                );
            return EvidenciaResult.erro(mensagem); //TODO: melhorar
        }
        catch(Exception e) {
            var mensagem = criarMensagemErro.apply(e.getMessage());
            log.error(mensagem);
            log.error(mensagem);
            Optional.ofNullable(relatorio.getInconformidades())
                .map(inconf -> inconf + ". " + mensagem)
                .ifPresentOrElse(
                        relatorio::setInconformidades,
                        () -> relatorio.setInconformidades(mensagem)
                );
            return EvidenciaResult.erro(mensagem); //TODO: melhorar
        }
        finally {
            log.info("Evidência {} finalizada.", process.getContexto());
        }
    }

    private Evidencia builExecRemessas(JobProcess process, Evidencia evidencia) {
        try {
            if(!process.getRemessasColetadas().isEmpty())
                log.info("Criando novos registros ExecFile para cada uma das saídas produzidas.");
            val saidas = process.getRemessasColetadas()
                .stream()
                .map(saida -> ExecFile.montarEvidenciaRemessa(evidencia, saida))
                .map(execFileService::save)
                .toList();
            evidencia.setRemessas(saidas);
        }
        catch(Exception e) {
            log.error("Erro ao tentar anexar as remessas na Evidência ID {}: {}",
                evidencia.getId(),
                e.getMessage());
            evidencia.setMensagemErro(e.getMessage() + ". " + evidencia.getMensagemErro());
        }
        return evidencia;
    }

    private Evidencia saveExecLogs(JobProcess process, Evidencia evidencia) {
        try {
            if(!process.getLogsColetados().isEmpty())
                log.info("Criando novos registros ExecFile para cada um dos logs obtidos.");
            var logs = process.getLogsColetados()
                .stream()
                .map(log -> ExecFile.montarEvidenciaLog(evidencia, log))
                .map(execFileService::save)
                .toList();
            evidencia.setLogs(logs);
        }
        catch(Exception e) {
            log.error("Erro ao tentar anexar os logs na Evidência ID {}: {}",
                evidencia.getId(),
                e.getMessage());
            evidencia.setMensagemErro(e.getMessage() + ". " + evidencia.getMensagemErro());
        }
        return evidencia;
    }

    private Evidencia saveExecQueries(JobProcess process, Evidencia evidencia) {
        try {
            val execQueries = new ArrayList<ExecQuery>();
            if(process.possuiTabelas())
                log.info("Criando novos registros ExecQuery para cada resultado no banco (pré e pós Job).");
            //TODO: Melhorar! Aplicar paralelismo!
            for(int i = 0; i < process.getTabelasPosJob().size(); i++) {
                val tabelaPre = process.getTabelasPreJob().get(i);
                val tabelaPos = process.getTabelasPosJob().get(i);
                if(!tabelaPre.getQuery().equals(tabelaPos.getQuery())) continue;

                val execQuery = ExecQuery.montarEvidencia(evidencia, tabelaPre, tabelaPos);
                execQueries.add(execQueryService.save(execQuery));
            }
            evidencia.setQueries(execQueries);
        }
        catch(Exception e) {
            log.error("Erro ao tentar anexar as queries na Evidência ID {}: {}",
                evidencia.getId(),
                e.getMessage());
            evidencia.setMensagemErro(e.getMessage() + ". " + evidencia.getMensagemErro());
        }
        return evidencia;
    }

    private Evidencia saveExecCargas(JobProcess process, Evidencia evidencia) {
        List<ExecFile> cargas = null;
        try {
            if(!process.getCargasEnviadas().isEmpty())
                log.info("Criando novos registros ExecFile para cada uma das cargas usadas.");
            cargas = process.getCargasColetadas()
                .stream()
                .map(carga -> ExecFile.montarEvidenciaCarga(evidencia, carga))
                .map(execFileService::save)
                .toList();
            evidencia.setCargas(cargas);
        }
        catch(Exception e) {
            log.error("Erro ao tentar anexar as cargas na Evidência ID {}: {}",
                evidencia.getId(),
                e.getMessage());
            evidencia.setMensagemErro(e.getMessage() + ". " + evidencia.getMensagemErro());
        }
        return evidencia;
    }

//    public File parseBlobToFile(@NonNull Blob blob, @NotBlank String filePath){
//        try(InputStream inputStream = blob.getBinaryStream()) {
//            log.info("Lendo os dados do Blob como uma String.");
//            byte[] bytes = inputStream.readAllBytes();
//            val jsonString = new String(bytes, StandardCharsets.UTF_8);
//
//            log.info("Salvando o Json em um arquivo no diretório: '{}'.", filePath);
//            try(val outputStream = new FileOutputStream(filePath)) {
//                val gson = new Gson();
//                val json = gson.toJson(jsonString);
//                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
//                log.info("Arquivo salvo com sucesso.");
//            }
//        }
//        catch(Exception e) {
//            log.warn("Falha ao tentar interpretar Blob: {}", e.getMessage());
//        }
//        return new File(filePath);
//    }


}
