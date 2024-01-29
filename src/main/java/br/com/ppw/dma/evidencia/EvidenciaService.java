package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ResultadoSql;
import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execFile.ExecFileService;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.execQuery.ExecQueryService;
import br.com.ppw.dma.job.JobExecutePOJO;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class EvidenciaService extends MasterService<Long, Evidencia, EvidenciaService> {

    @Autowired
    private final EvidenciaRepository evidenciaDao;

    @Autowired
    private final ExecFileService execFileService;

    @Autowired
    private final ExecQueryService execQueryService;

    @Autowired
    private Gson gson;

    public EvidenciaService(
        EvidenciaRepository evidenciaDao,
        ExecFileService execFileService,
        ExecQueryService execQueryService,
        Gson gson) {
        //---------------------------------------
        super(evidenciaDao);
        this.evidenciaDao = evidenciaDao;
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
    public List<ResultadoSql> extractTablePreJob(
        @NotEmpty List<? extends ComandoSql> comandosSql,
        @NonNull AmbienteAcessoDTO ambienteBanco) {
        //---------------------------------------------------
        val extracoes = comandosSql.stream().map(cmdSql -> {
            try {
                log.info("Extraindo tabela '{}' pré-Job.", cmdSql.getTabela());
                return extractTable(cmdSql, ambienteBanco);
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
    public List<ResultadoSql> extractTablePosJob(
        @NonNull List<ResultadoSql> resultadoSqls,
        @NonNull AmbienteAcessoDTO ambienteBanco) {
        //---------------------------------------
        val extracoes = resultadoSqls.stream().map(rSql -> {
            try {
                log.info("Extraindo tabela '{}' pós-Job.", rSql.getTabela());
                return extractTable(rSql, ambienteBanco);
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
    private ResultadoSql extractTable(@NonNull ComandoSql comandoSql, @NonNull AmbienteAcessoDTO banco)
    throws SQLException {
        try(val masterDao = new MasterOracleDAO(banco)) {
            return masterDao.getAllInfoFromTable(new ResultadoSql(comandoSql))
                .fecharConsultaPreJob();
        }
    }

    //TODO: javadoc
    private ResultadoSql extractTable(@NonNull ResultadoSql resultadoSql, @NonNull AmbienteAcessoDTO banco)
    throws SQLException {
        try(val masterDao = new MasterOracleDAO(banco)) {
            return masterDao.getAllInfoFromTable(resultadoSql);
        }
    }

    //TODO: javadoc
    @Transactional
    public Evidencia createEvidencia(@NonNull JobExecutePOJO jobPojo) {
        log.info("Iniciando processo de geração de Evidência com base na JobExecutePOJO:");
        log.info(jobPojo.toString());
        val logs = new ArrayList<ExecFile>();

        log.info("Criando novo registro da Evidência.");
        val evidencia = persist(Evidencia.jobPojoExecutado(jobPojo));
        evidenciaDao.flush();

        if (!jobPojo.getTabelas().isEmpty())
            log.info("Criando novos registros ExecQuery para cada resultado no banco (pré e pós Job).");
        val banco = jobPojo.getTabelas()
            .stream()
            .map(tabela -> ExecQuery.montarEvidencia(evidencia, tabela))
            .map(execQueryService::persist)
            .toList();

        if (!jobPojo.getCargas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das cargas usadas.");
        val cargas = jobPojo.getCargas()
            .stream()
            .map(carga -> ExecFile.montarEvidenciaCarga(evidencia, carga))
            .map(execFileService::persist)
            .toList();

        if (!jobPojo.getLogs().isEmpty() || !jobPojo.getTerminal().isEmpty())
            log.info("Criando novos registros ExecFile para cada um dos logs obtidos.");

        final String terminalConteudo = jobPojo.getTerminalFormatado();
        if (!terminalConteudo.isEmpty()) {
            val execFileTerminal = ExecFile.montarEvidenciaTerminal(evidencia, terminalConteudo);
            logs.add(execFileTerminal);
        }
        jobPojo.getLogs()
            .stream()
            .map(log -> ExecFile.montarEvidenciaLog(evidencia, log))
            .map(execFileService::persist)
            .forEach(logs::add);

        if (!jobPojo.getSaidas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das saídas produzidas.");
        val saidas = jobPojo.getSaidas()
            .stream()
            .map(saida -> ExecFile.montarEvidenciaSaida(evidencia, saida))
            .map(execFileService::persist)
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
