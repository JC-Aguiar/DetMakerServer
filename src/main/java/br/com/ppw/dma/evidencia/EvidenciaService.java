package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execFile.ExecFileService;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.execQuery.ExecQueryService;
import br.com.ppw.dma.job.JobExecutePOJO;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.ResultadoSql;
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
        validateInputs(comandoSql);
        val resultadoSql = new ResultadoSql(comandoSql);
        return oracleDao.getFieldAndValuesFromTable(resultadoSql);
    }

    //TODO: javadoc
    private ResultadoSql extractTable(@NonNull ResultadoSql resultadoSql) {
        validateInputs(resultadoSql);
        return oracleDao.getFieldAndValuesFromTable(resultadoSql);
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
                .argumentos(jobPojo.getArgumentos())
                .dataInicio(jobPojo.getDataInicio())
                .dataFim(jobPojo.getDataFim())
                .sucesso(jobPojo.isSucesso())
                .build()
        );
        evidenciaDao.flush();

        if(!jobPojo.getCargas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das cargas usadas.");
        val cargas = jobPojo.getCargas()
            .stream()
            .map(carga -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .arquivoNome(carga.getName())
                    .arquivo(lerArquivo(carga))
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

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
                    .resultadoPreJob(tabela.getResumoPreJob())
                    .resultadoPosJob(tabela.getResumoPosJob())
                    //TODO: ?informações da pipeline?
                    .build();
                return execQueryService.persist(execQuery);
            })
            .toList();

        if(!jobPojo.getLogs().isEmpty())
            log.info("Criando novos registros ExecFile para cada umo dos logs obtidos.");
        val logs = jobPojo.getLogs()
            .stream()
            .map(carga -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .jobNome(jobPojo.getJob().getNome())
                    .arquivoNome(carga.getName())
                    .arquivo(lerArquivo(carga))
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        if(!jobPojo.getSaidas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das saídas produzidas.");
        val saidas = jobPojo.getSaidas()
            .stream()
            .map(carga -> {
                val execFile = ExecFile.builder()
                    .evidencia(evidencia)
                    .jobNome(jobPojo.getJob().getNome())
                    .arquivoNome(carga.getName())
                    .arquivo(lerArquivo(carga))
                    .build();
                return execFileService.persist(execFile);
            })
            .toList();

        log.info("Atualizando Evidência ID {} com os anexos (ExecFile e ExecQuery).", evidencia.getId());
        evidencia.setCargas(cargas);
        evidencia.setBanco(banco);
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

    public EvidenciaInfoDTO parseToResponseDto(@NonNull Evidencia evidencia, @NonNull Integer ordem) {
        log.info("Convertendo entidade Evidência para DTO de resposta");
        List<String> queries = new ArrayList<>();
        List<String> tabelasNome = new ArrayList<>();
        List<String> bancoPreJob = new ArrayList<>();
        List<String> bancoPosJob = new ArrayList<>();

        for(val execQuery : evidencia.getBanco()) {
            queries.add(execQuery.getQuery());
            tabelasNome.add(execQuery.getTabelaNome());
            bancoPreJob.add(execQuery.getResultadoPreJob());
            bancoPosJob.add(execQuery.getResultadoPosJob());
        }
        //TODO: refatorar para otimizar a iteração nas listas durante preenchimento do EvidenciaInfoDTO
        val dto = EvidenciaInfoDTO.builder()
            .id(evidencia.getId())
            .job(evidencia.getJob().getNome())
            .jobDescricao(evidencia.getJob().getDescricao())
            .data(evidencia.getDataInicio())
            .sucesso(evidencia.getSucesso())
            .ordem(ordem)
            .argumentos(evidencia.getArgumentos())
            .queries(queries)
            .tabelasNome(tabelasNome)
            .tabelasPreJob(bancoPreJob)
            .tabelasPosJob(bancoPosJob)
            .cargas(AnexoInfoDTO.tipoCarga(evidencia.getCargas()))
            .logs(AnexoInfoDTO.tipoLog(evidencia.getLogs()))
            .saidas(AnexoInfoDTO.tipoProduto(evidencia.getSaidas()))
            .build();

        log.info(dto.toString());
        return dto;
    }
}
