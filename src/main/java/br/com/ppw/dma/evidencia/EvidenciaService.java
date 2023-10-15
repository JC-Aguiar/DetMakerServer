package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execFile.ExecFileService;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.execQuery.ExecQueryService;
import br.com.ppw.dma.job.JobExecutePOJO;
import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.ResultadoSql;
import br.com.ppw.dma.master.MasterOracleDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class EvidenciaService {

    @Autowired
    private final EvidenciaRepository evidenciaDao;

    @Autowired
    private final MasterOracleDAO oracleDao;

    @Autowired
    private final ExecFileService execFileService;

    @Autowired
    private final ExecQueryService execQueryService;

    public EvidenciaService(
        EvidenciaRepository evidenciaDao,
        MasterOracleDAO oracleDao,
        ExecFileService execFileService,
        ExecQueryService execQueryService) {
        //---------------------------------------
        this.evidenciaDao = evidenciaDao;
        this.oracleDao = oracleDao;
        this.execFileService = execFileService;
        this.execQueryService = execQueryService;
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
        val resultado = extractTable(cmdSql);
        return new ResultadoSql(cmdSql).addResultadoPreJob(resultado);
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
        val resultado = extractTable(resultSql);
        return resultSql.addResultadoPosJob(resultado);
    }

    //TODO: javadoc
    private List<Map<String, Object>> extractTable(@NonNull ComandoSql comandoSql) {
        validateInputs(comandoSql);
        val campos = checkFieldValues(comandoSql);
        return oracleDao.getFieldAndValuesFromTable(
            campos, comandoSql.getTabela(), comandoSql.getFiltros()
        );
    }

    //TODO: criar exception própria
    //TODO: javadoc
    public void validateInputs(@NonNull ComandoSql sql) {
        log.info("Validando valores preenchidos para campos, tabela e filtros.");
        boolean camposValidos = validateQuery(sql.getCampos());
        boolean tabelaValida = validateQuery(sql.getTabela());
        boolean filtroValido = validateQuery(sql.getFiltros());
        if(!camposValidos || !tabelaValida || !filtroValido) {
            throw new RuntimeException("A queries informada contêm comandos DDL não permitidos.");
        }
    }

    public List<String> checkFieldValues(@NonNull ComandoSql sql) {
        List<String> campos = null;
        if(sql.semCampos()) campos = oracleDao.getFieldsFromTable(sql.getTabela());
        else campos = sql.getCampos();
        return campos;
    }

    //TODO: javadoc
    public boolean validateQuery(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        String ddlPattern = "(?i)\\b(create|alter|drop|truncate|rename)\\b";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public boolean validateQuery(List<String> campos) {
        if(campos == null || campos.isEmpty()) return true;
        return campos.stream().allMatch(this::validateQuery);
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
                .build()
        );
        log.info("Evidência ID: {}.", evidencia.getId());
        evidenciaDao.flush();

        log.info("Criando novos registros ExecFile para cada uma das cargas usadas.");
        val cargas = jobPojo.getCargas()
            .stream()
            .map(carga -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .nome(carga.getName())
                    .arquivo(carga)
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        log.info("Criando novos registros ExecQuery para cada uma das consultas pre-execução.");
        val queriesPreJob = jobPojo.getTabelas()
            .stream()
            .map(tabela -> {
                val preJob = tabela.getTabelasPreJob();
                val execQuery = ExecQuery.builder()
                    .evidencia(evidencia)
                    .tabelaNome(tabela.getTabela())
                    .query(tabela.getSqlCompleta())
                    .resultado(parseTableToBlob(preJob))
                    .build();
                return execQueryService.persist(execQuery);
            })
            .toList();

        log.info("Criando novos registros ExecQuery para cada uma das consultas pós-execução.");
        val queriesPosJob = jobPojo.getTabelas()
            .stream()
            .map(tabela -> {
                val preJob = tabela.getTabelasPosJob();
                val execQuery = ExecQuery.builder()
                    .evidencia(evidencia)
                    .tabelaNome(tabela.getTabela())
                    .query(tabela.getSqlCompleta())
                    .resultado(parseTableToBlob(preJob))
                    .build();
                return execQueryService.persist(execQuery);
            })
            .toList();

        log.info("Criando novos registros ExecFile para cada umo dos logs obtidos.");
        val logs = jobPojo.getLogs()
            .stream()
            .map(carga -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .nome(carga.getName())
                    .arquivo(carga)
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        log.info("Criando novos registros ExecFile para cada uma das saídas produzidas.");
        val saidas = jobPojo.getProdutos()
            .stream()
            .map(carga -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .nome(carga.getName())
                    .arquivo(carga)
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        //execFileService...flush?
        //execQueryService...flush?

        log.info("Relacionando Evidência com os anexos (ExecFile e ExecQuery).");
        evidencia.setCargas(cargas);
        evidencia.setBancoPreJob(queriesPreJob);
        evidencia.setBancoPosJob(queriesPosJob);
        evidencia.setLogs(logs);
        evidencia.setSaidas(saidas);
        return persist(evidencia);
    }

    //TODO: javadoc
    //TODO: ainda necessário?
    public EvidenciaResponseDTO createEvidenciaDto(@NonNull JobExecutePOJO pilhaDTO) {
        log.info("Gerando EvidenciaResponseDTO com base na JobExecutePOJO:");
        log.info(pilhaDTO.toString());

        val evidencia = EvidenciaResponseDTO.builder()
            .job(pilhaDTO.getJobInfo().getNome())
            .sucesso(pilhaDTO.isSucesso())
            .argumentos(pilhaDTO.getParametro())
            .queries(pilhaDTO.getTabelas()
                .stream()
                .map(ResultadoSql::getSqlCompleta)
                .toList())
            .cargas(pilhaDTO.getCargas())
            .logs(pilhaDTO.getLogs())
            .saidas(pilhaDTO.getProdutos())
            .build();

        log.info("EvidenciaResponseDTO gerada:");
        log.info(evidencia.toString());
        return evidencia;
    }

    @SneakyThrows
    public Blob parseTableToBlob(@NonNull List<Map<String, Object>> list) {
        val baos = new ByteArrayOutputStream();
        val oos = new ObjectOutputStream(baos);
        oos.writeObject(list);
        byte[] bytes = baos.toByteArray();
        return new SerialBlob(bytes);
    }

    @SneakyThrows
    public File parseBlogToFile(@NonNull Blob blob, @NotBlank String filePath){
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
        return new File(filePath);
    }

    public EvidenciaResponseDTO parseToResponseDto(@NonNull Evidencia evidencia) {
        return EvidenciaResponseDTO.builder()
            .job(evidencia.getJob().getNome())
            .sucesso(evidencia.getSucesso())
            .argumentos(evidencia.getArgumentos())
            .queries(
                evidencia.getBancoPreJob()
                    .stream()
                    .map(ExecQuery::getQuery)
                    .toList())
            .tabelas(
                evidencia.getBancoPreJob()
                    .stream()
                    .map(ExecQuery::getResultado)
                    .map(blob -> parseBlogToFile(blob, "diretorio_algo"))//TODO: ajustar!
                    .toList())
            .cargas(
                evidencia.getCargas()
                    .stream()
                    .map(ExecFile::getArquivo)
                    .toList())
            .logs(
                evidencia.getLogs()
                    .stream()
                    .map(ExecFile::getArquivo)
                    .toList())
            .saidas(
                evidencia.getSaidas()
                    .stream()
                    .map(ExecFile::getArquivo)
                    .toList())
            .build();
    }
}
