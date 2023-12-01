package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execFile.ExecFileService;
import br.com.ppw.dma.execFile.TipoExecFile;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.execQuery.ExecQueryService;
import br.com.ppw.dma.job.JobExecutePOJO;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ResultadoSql;
import br.com.ppw.dma.util.ValidadorSQL;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static br.com.ppw.dma.execFile.TipoExecFile.*;
import static br.com.ppw.dma.system.Arquivos.lerArquivo;

@Service
@Slf4j
public class EvidenciaService extends MasterService<Long, Evidencia, EvidenciaService> {

    @Autowired
    private final EvidenciaRepository evidenciaDao;

    @Autowired
    private final MasterOracleDAO oracleDao;

    @Autowired
    private final ExecFileService execFileService;

    @Autowired
    private final ExecQueryService execQueryService;

    @Autowired
    private Gson gson;

    public EvidenciaService(
        EvidenciaRepository evidenciaDao,
        MasterOracleDAO oracleDao,
        ExecFileService execFileService,
        ExecQueryService execQueryService,
        Gson gson) {
        //---------------------------------------
        super(evidenciaDao);
        this.evidenciaDao = evidenciaDao;
        this.oracleDao = oracleDao;
        this.execFileService = execFileService;
        this.execQueryService = execQueryService;
        this.gson = gson;
    }

    @Transactional
    public Evidencia persist(@NotNull Evidencia evidencia) {
        log.info("Persistindo Evidencia no banco:");
        log.info(evidencia.toString());
        evidencia = evidenciaDao.save(evidencia);

        log.info("Evidência ID {} gravado com sucesso.", evidencia.getId());
        return evidencia;
    }

    //TODO: javadoc
    public List<ResultadoSql> extractTablePreJob(@NotEmpty List<? extends ComandoSql> comandosSql) {
        val extracoes = comandosSql.stream().map(cmdSql -> {
            try {
                return extractTablePreJob(cmdSql);
            }
            catch(Exception e) {
                e.printStackTrace();
                log.warn(e.getMessage());
                return null;
            }})
            .filter(Objects::nonNull)
            .toList();

        log.info("Total de comandos SQL realizados: {}.", extracoes.size());
        return extracoes;
    }

    //TODO: javadoc
    public ResultadoSql extractTablePreJob(@NonNull ComandoSql cmdSql) {
        log.info("Realizando extração pré-job da tabela '{}'.", cmdSql.getTabela());
        return extractTable(cmdSql);
    }

    //TODO: javadoc
    public List<ResultadoSql> extractTablePosJob(@NonNull List<ResultadoSql> resultadoSqls) {
        val extracoes = resultadoSqls.stream().map(sql -> {
            try {
                return extractTablePosJob(sql);
            }
            catch(Exception e) {
                e.printStackTrace();
                log.warn(e.getMessage());
                return null;
            }})
            .filter(Objects::nonNull)
            .toList();

        log.info("Total de comandos SQL realizados: {}.", extracoes.size());
        return extracoes;
    }

    //TODO: javadoc
    public ResultadoSql extractTablePosJob(@NonNull ResultadoSql resultSql) {
        log.info("Realizando extração pós-job da tabela '{}'.", resultSql.getTabela());
        return extractTable(resultSql);
    }

    //TODO: javadoc
    private ResultadoSql extractTable(@NonNull ComandoSql comandoSql) {
        val resultadoSql = new ResultadoSql(comandoSql);
        return oracleDao.getAllInfoFromTable(resultadoSql);
    }

    //TODO: javadoc
    private ResultadoSql extractTable(@NonNull ResultadoSql resultadoSql) {
        return oracleDao.getAllInfoFromTable(resultadoSql);
    }

    //TODO: javadoc
    @Transactional
    public Evidencia createEvidencia(@NonNull JobExecutePOJO jobPojo) {
        log.info("Iniciando processo de geração de Evidência com base na JobExecutePOJO:");
        log.info(jobPojo.toString());

        log.info("Criando novo registro da Evidência.");
        val evidencia = persist(
            Evidencia.builder()
                .job(jobPojo.getJob())
                .sucesso(jobPojo.isSucesso())
                .ordem(jobPojo.getOrdem())
                .argumentos(jobPojo.getArgumentos())
                .dataInicio(jobPojo.getDataInicio())
                .dataFim(jobPojo.getDataFim())
                .sucesso(jobPojo.isSucesso())
                .build()
        );
        evidenciaDao.flush();

        if(!jobPojo.getTabelas().isEmpty())
            log.info("Criando novos registros ExecQuery para cada resultado no banco (pré e pós Job).");
        val banco = jobPojo.getTabelas()
            .stream()
            .map(tabela -> {
                val execQuery = ExecQuery.builder()
                    .evidencia(evidencia)
                    .jobNome(jobPojo.getJob().getNome())
                    .tabelaNome(tabela.getTabela())
                    .query(tabela.getSqlCompleta())
                    .resultadoPreJob(tabela.resumoPreJob())
                    .resultadoPosJob(tabela.resumoPosJob())
                    //TODO: ?informações da pipeline?
                    .build();
                return execQueryService.persist(execQuery);
            })
            .toList();

        if(!jobPojo.getCargas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das cargas usadas.");
        val cargas = jobPojo.getCargas()
            .stream()
            .map(carga -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .jobNome(evidencia.getJob().getNome())
                    .tipo(CARGA)
                    .arquivoNome(carga.getName())
                    .arquivo(lerArquivo(carga))
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        if(!jobPojo.getLogs().isEmpty())
            log.info("Criando novos registros ExecFile para cada umo dos logs obtidos.");
        val logs = jobPojo.getLogs()
            .stream()
            .map(log -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .jobNome(jobPojo.getJob().getNome())
                    .tipo(LOG)
                    .arquivoNome(log.getName())
                    .arquivo(lerArquivo(log))
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        if(!jobPojo.getSaidas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das saídas produzidas.");
        val saidas = jobPojo.getSaidas()
            .stream()
            .map(saida -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .jobNome(jobPojo.getJob().getNome())
                    .tipo(SAIDA)
                    .arquivoNome(saida.getName())
                    .arquivo(lerArquivo(saida))
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        log.info("Atualizando Evidência ID {} com os anexos (ExecFile e ExecQuery).", evidencia.getId());
        evidencia.setBanco(banco);
        evidencia.setCargas(cargas);
        evidencia.setLogs(logs);
        evidencia.setSaidas(saidas);
        return evidencia;
    }

    public File parseBlobToFile(@NonNull Blob blob, @NotBlank String filePath){
        try(InputStream inputStream = blob.getBinaryStream()) {
            log.info("Lendo os dados do Blob como uma String.");
            byte[] bytes = inputStream.readAllBytes();
            val jsonString = new String(bytes, StandardCharsets.UTF_8);

            log.info("Salvando o Json em um arquivo no diretório: '{}'.", filePath);
            try(val outputStream = new FileOutputStream(filePath)) {
                val gson = new Gson();
                val json = gson.toJson(jsonString);
                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
                log.info("Arquivo salvo com sucesso.");
            }
        }
        catch(Exception e) {
            log.warn("Falha ao tentar interpretar Blob: {}", e.getMessage());
        }
        return new File(filePath);
    }


}
