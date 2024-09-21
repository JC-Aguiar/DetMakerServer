package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.execFile.ExecFileService;
import br.com.ppw.dma.domain.execQuery.ExecQuery;
import br.com.ppw.dma.domain.execQuery.ExecQueryService;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.queue.result.EvidenciaResult;
import br.com.ppw.dma.domain.queue.result.JobResult;
import br.com.ppw.dma.domain.queue.result.PipelineResult;
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
import java.util.function.Function;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Service
@Slf4j
public class EvidenciaService extends MasterService<Long, Evidencia, EvidenciaService> {

    private final EvidenciaRepository evidenciaDao;
    private final ExecFileService execFileService;
    private final ExecQueryService execQueryService;
    private final JobService jobService;
    private final Gson gson;

    @Autowired
    public EvidenciaService(
        EvidenciaRepository evidenciaDao,
        ExecFileService execFileService,
        ExecQueryService execQueryService,
        JobService jobService,
        Gson gson) {
        //---------------------------------------
        super(evidenciaDao);
        this.evidenciaDao = evidenciaDao;
        this.execFileService = execFileService;
        this.execQueryService = execQueryService;
        this.jobService = jobService;
        this.gson = gson;
    }


    //TODO: javadoc
//    @Transactional(noRollbackFor = Throwable.class)
    public List<EvidenciaResult> gerarEvidencia(@NonNull PipelineResult pipelineResult) {
        var jobsResult = pipelineResult.getResultadoJobs();
        log.info("Iniciando geração de Evidências para {} registro(s).", jobsResult.size());
        return jobsResult.stream()
            .map(this::gerarEvidencia)
            .toList();
    }

    //TODO: javadoc
//    @Transactional(noRollbackFor = Throwable.class)
    public EvidenciaResult gerarEvidencia(@NonNull JobResult process) {
        log.info("Gerando Evidência para {}.", process.getContexto());
        val logs = new ArrayList<ExecFile>();
        Function<String, String> criarMensagemErro = (erro) -> String.format(
            "Erro na criação da Evidência para o %s: %s",
            process.getContexto(),
            erro);

        try {
//            log.info("Coletando entidade do Job no banco.");
//            var job = jobService.findById(process.getJobInfo().getId());
            var evidencia = save(new Evidencia(process));
            evidenciaDao.flush();
            log.info(evidencia.toString());

            val banco = new ArrayList<ExecQuery>();
            if (process.possuiTabelas())
                log.info("Criando novos registros ExecQuery para cada resultado no banco (pré e pós Job).");
            //TODO: Melhorar! Aplicar paralelismo!
            for (int i = 0; i < process.getTabelasPosJob().size(); i++) {
                val tabelaPre = process.getTabelasPreJob().get(i);
                val tabelaPos = process.getTabelasPosJob().get(i);
                if (!tabelaPre.getQuery().equals(tabelaPos.getQuery())) continue;
                val query = ExecQuery.montarEvidencia(evidencia, tabelaPre, tabelaPos);
                banco.add(execQueryService.save(query));
            }

            if (!process.getCargasColetadas().isEmpty())
                log.info("Criando novos registros ExecFile para cada uma das cargas usadas.");
            val cargas = process.getCargasColetadas()
                .stream()
                .map(carga -> ExecFile.montarEvidenciaCarga(evidencia, carga))
                .map(execFileService::save)
                .toList();

            if (!process.getLogsColetados().isEmpty() || !process.getTerminalFormatado().isEmpty())
                log.info("Criando novos registros ExecFile para cada um dos logs obtidos.");
            if (!process.getTerminalFormatado().isEmpty()) {
                val execFileTerminal = ExecFile.montarEvidenciaTerminal(evidencia, process.getTerminal());
                logs.add(execFileTerminal);
            }
            process.getLogsColetados()
                .stream()
                .map(log -> ExecFile.montarEvidenciaLog(evidencia, log))
                .map(execFileService::save)
                .forEach(logs::add);

            if (!process.getRemessasColetadas().isEmpty())
                log.info("Criando novos registros ExecFile para cada uma das saídas produzidas.");
            val saidas = process.getRemessasColetadas()
                .stream()
                .map(saida -> ExecFile.montarEvidenciaRemessa(evidencia, saida))
                .map(execFileService::save)
                .toList();

            log.info("Atualizando Evidência ID {} com os anexos (ExecFile e ExecQuery).", evidencia.getId());
            addEvidenciaBanco(evidencia, banco);
            addEvidenciaCarga(evidencia, cargas);
            addEvidenciaLog(evidencia, logs);
            addEvidenciaRemessa(evidencia, saidas);

            if (evidencia.getMensagemErro() == null || evidencia.getMensagemErro().isEmpty())
                return EvidenciaResult.ok(evidencia);  //TODO: melhorar

            evidencia.setRevisor("Det-Maker");
            evidencia.setDataRevisao(OffsetDateTime.now(RELOGIO));
            evidencia.setStatus(TipoEvidenciaStatus.REPROVADO);
            evidencia.setComentario("O Job obteve algum tipo de erro " +
                "e seu resultado foi definido automaticamente");
            return EvidenciaResult.ok(evidencia);  //TODO: melhorar
        }
        catch(PersistenceException e) {
            var mensagem = criarMensagemErro.apply(SqlSintaxe.getExceptionMainCause(e));
            log.error(mensagem);
            return EvidenciaResult.erro(mensagem); //TODO: melhorar
        }
        catch(Exception e) {
            var mensagem = criarMensagemErro.apply(e.getMessage());
            log.error(mensagem);
            return EvidenciaResult.erro(mensagem); //TODO: melhorar
        }
        finally {
            log.info("Evidência {} finalizada.", process.getContexto());
        }
    }

    private static void addEvidenciaRemessa(
        @NonNull Evidencia evidencia,
        @NonNull List<ExecFile> saidas) {

        try {
            evidencia.setRemessas(saidas);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void addEvidenciaLog(
        @NonNull Evidencia evidencia,
        @NonNull ArrayList<ExecFile> logs) {

        try {
            evidencia.setLogs(logs);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void addEvidenciaCarga(
        @NonNull Evidencia evidencia,
        @NonNull List<ExecFile> cargas) {

        try {
            evidencia.setCargas(cargas);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    private static void addEvidenciaBanco(
        @NonNull Evidencia evidencia,
        @NonNull ArrayList<ExecQuery> banco) {

        try {
            evidencia.setQueries(banco);
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }
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
