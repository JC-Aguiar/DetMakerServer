package br.com.ppw.dma;

import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.jobQuery.ResultadoSql;
import br.com.ppw.dma.domain.master.*;
import br.com.ppw.dma.domain.pipeline.execution.PipelineExecDTO;
import br.com.ppw.dma.domain.task.TaskPushResponseDTO;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.util.BashSintaxe;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.domain.master.SqlSintaxe.getExceptionMainCause;
import static br.com.ppw.dma.domain.master.SqlSintaxe.isSafeSelect;
import static br.com.ppw.dma.util.FormatString.dividirValores;

@Slf4j
public class BasicTest {


//    @Test
//    public void testandoQueues2() throws Exception {
//        var threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
//        threadPoolTaskScheduler.setPoolSize(5);
//        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
//        threadPoolTaskScheduler.execute();
//    }

    @Test
    public void testarMontarTabelaString() {
        var tabela = new ArrayList<List<Object>>();
        var campos = List.<Object>of(
            "EVDTPROC",
            "EVACCTG",
            "EVOBJ",
            "EVDTSOLIC",
            "EVDTREG",
            "EVTYPE",
            "EVSTATUS",
            "EVEXPDESC",
            "EVID",
            "EVACCT",
            "EVTOPIC");
        tabela.add(campos);
        var valores = List.of(
            "2024-06-11 16:02:31.136",
            "1",
            new ModelMapper(),
            "2024-06-09 10:11:58.0",
            "2024-06-09 10:11:58.0",
            "EV_PAYMENTMODEL",
            "9",
            Optional.empty(),
            11751,
            "220853717-BRM",
            Optional.empty());
        tabela.add(valores);
        var tabelaString = FormatString.tabelaParaString(tabela);
        System.out.println(tabelaString);

        var resultadoSql = new ResultadoSql("Teste", "Nada", "Alguma query DML");
        var resultadoMock = Map.of(
            "EVDTPROC", "2024-06-11 16:02:31.136",
            "EVACCTG", "1",
            "EVOBJ", new ModelMapper(),
            "EVDTSOLIC", "2024-06-09 10:11:58.0",
            "EVDTREG", "2024-06-09 10:11:58.0",
            "EVTYPE", "EV_PAYMENTMODEL",
            "EVSTATUS", "9",
            "EVEXPDESC", Optional.empty(),
            "EVID", 11751,
            "EVACCT", "220853717-BRM");
        resultadoSql.addResultado(List.of(resultadoMock));
        tabelaString = resultadoSql.getResultadoAsString();
        System.out.println(tabelaString);
    }

    @Test
    public void testandoQueues() throws Exception {
        // Fila de eventos
        var fila = new ConcurrentLinkedQueue<TaskPushResponseDTO>();
        var filaProcessador = Executors.newSingleThreadExecutor();
        var registros = 20;

        log.info("Iniciando fila de tratamento.");
        filaProcessador.execute(() -> {
            while(true) {
                synchronized(fila) {
                    if(fila.isEmpty()) continue;
                    try {
                        log.info("Item na fila identificado.");
                        var queueDto = fila.poll();
                        log.info(queueDto.toString());
                        Thread.sleep(Duration.ofSeconds(2).toMillis());
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                log.info("rodando.");
            }
        });

        log.info("Inserindo {} registro(s) de uma vez só.", registros);
        var index = new AtomicInteger(0);
        Stream.generate(index::incrementAndGet).limit(registros).forEach(nada -> {
            var itemFila = TaskPushResponseDTO.builder()
                .ambienteId(1)
                .queueSize(index.get())
                .ticket(UUID.randomUUID().toString())
                .build();
            fila.offer(itemFila);
        });
        log.info("Inserções finalizadas.");

        filaProcessador.awaitTermination(1, TimeUnit.MINUTES);
//        processingThread.start();
//        processingThread.join();
    }

    @Test
    public void converterData() throws Exception {
        String dataExecString = "2024-05-24";
        var dataExec = LocalDate.parse(dataExecString);
        log.info("Data Exec String: {}", dataExecString);
        log.info("Data Exec: {}", dataExec);
    }

    @Test
    public void testeInterpretadorDeMascaraDeArquivo() throws IOException {
        var mascaras = List.of(
            "proximas_parcelas_{DATA}_{HORA}.json",
            "proximas_parcelas_<DATA>_<HORA>.log",
            "proximas_parcelas_DATA_HORA.log",
            "CY3_CARGA_IE001_AAAAMMDD_HHMMSS.log",
            "EVENTOS_WEB001_AAAAMMDD_HHMMSS.txt",
            "proc_IE001_delq_uda_IE001_S0_AAAAMMDD.log",
            "cy3_carga_atributos_4p_fase1_AAAAMMDD_HHMMSS.log",
            "cy3_carga_atributos_4p_fase2_AAAAMMDD_HHMMSS.log",
            "cy3_4p_upd_historico_AAAAMMDD_HHMMSS.log",
            "cy3_4p_upd_cobranca_fase2_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_IE008_AAAAMMDD_HHMMSS.log",
            "Cy3_CARGA_IE007_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_IE002_TEMP_FATURAS_AAAAMMDD_HHMMSS.log",
            "CY3_VALIDA_IE012_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_IE006_ADRESS_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_IE006_PHONE_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_TABELA_760_IE008_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_NOVAS_FATURAS_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_INDIC_VAL_IE023_AAAAMMDD_HHMMSS.log",
            "CY3_MANFAT_754_IE002_AAAAMMDD_HHMMSS.log",
            "cy3_atualiza_PARAM_TABLE_AAAAMMDD_HHMMSS.log",
            "sql_upd_PARAM_TABLE_AAAAMMDD_HHMMSS.log",
            "CY3_DELQUDA_IE001_AAAAMMDD_HHMMSS.log",
            "CY3_DELQUDA_IE008_AAAAMMDD_HHMMSS.log",
            "GVT_CARGA_TABELA_780_IE012_AAAAMMDD_HHMMSS.log",
            "cy3_4p_upd_cobranca_fase1_AAAAMMDD_HHMMSS.log",
            "CY3_CARGA_TABELA_775_IE007_AAAAMMDD_HHMMSS.log",
            "EVENTOS_WEB_017_AAMMDD_HHMMSS.log",
            "CY3_PREP_ESTACORDO_AAMMDD_HHMMSS.log",
            "PROC_BEB385_PREP_ESTACORDOAAMMDD.log",
            "CY3_ARRECADACAO_AAMMDD_HHMMSS.log",
            "cy3_ARRECADACAO_AAMMDD_HHMMSS.log",
            "CY3_MANFAT_755_F1_IE002_AAAAMMDD_HHMMSS.log",
            "cy3_shell_kafka_producer016_DATA_HORA.log",
            "EVENTOS_PRODUCER_016_DATA_HORA.log",
            "CY3_CARGA_TABELAS_IE023_delquda_1_indicador_AAAAMMDD_HHMMSS.log",
            "proc_fluxo_quebra_acordo_AAAAMMDD_F{NUMEROFASE}_S{NUMEROSUBSET}.log",
            "proc_fluxo_quebra_acordo_AAAAMMDD_F{NUMEROFASE}_S{NUMEROSUBSET}.log",
            "proc_atualiza_dmdays_AAAAMMDD.log",
            "cy3_proc_Queue3_AAAAMMDD_HHMMSS.log",
            "cy3_Crit_Sel_Lbl_AAAAMMDD_HHMMSS.log",
            "cy3_Crit_Selecao_AAAAMMDD_HHMMSS.log",
            "cy3_gestao_acion_etiquetas_AAMMDD.log",
            "cy3_Distribuicao_AAAAMMDD_HHMMSS.log",
            "cy3_arrasto_distribuicao_E{ETAPA_PROC}_AAAAMMDD.log",
            "cy3_arrasto_distribuicao_E{ETAPA_PROC}_AAAAMMDD.log",
            "cy3_Crit_Acordo_AAAAMMDD_HHMMSS.log",
            "cy3_proc_Queue4_AAAAMMDD_HHMMSS.log",
            "cy3_Crit_Comissionamento_AAAAMMDD_HHMMSS.log",
            "cy3_Crit_Neg_Reab_AAAAMMDD_HHMMSS.log",
            "cy3_historico_distribuicao_AAAAMMDD.log",
            "cy3_historico_distribuicao_AAAAMMDD_SSUBSET.log",
            "cy3_proc_bloqueioAAAAMMDD.log",
            "proc_bloqueioAAAAMMDD_SSUBSET.log",
            "cy3_proc_desbloqueioAAAAMMDD.log",
            "proc_desbloqueioAAAAMMDD_SSUBSET.log",
            "cy3_atualiza_perfil_usuario_AAAAMMDD_HHMISS.log",
            "cy3_atualiza_perfil_usuario_AAAAMMDD_HHMISS_sql.log",
            "proc_atualiza_uda_AAAAMMDD.log",
            "cy3_atualiza_PARAM_TABLE_AAAAMMDD_HHMMSS.log",
            "sql_upd_PARAM_TABLE_AAAAMMDD_HHMMSS.log",
            "cy3_Saida_IS001_BLOQUEIO_AAAAMMDD_HHMMSS.log",
            "proc_pr_ppw_hist_ie012_AAAAMMDD_HHMMSS.log",
            "proc_prc_ppw_hist_const_IE012_AAAAMMDD_HHMMSS.log",
            "cy3_shell_gera_boleto_745AAAAMMDD.log",
            "proc_beb745_baixa_pagto_cnabAAAAMMDD.log",
            "cy3_Saida_IS006_ASSESSORIAS_AAAAMMDD_HHMMSS.log",
            "cy3_Saida_IS006B_ASSESSORIAS_AAAAMMDD_HHMMSS.log",
            "cy3_Saida_SAP_IS007_AAAAMMDD_HHMMSS.log",
            "cy3_Saida_BRM_fat_neg_AAAAMMDD_HHMMSS.log",
            "cy3_Saida_BRM_atu_venc_fat_AAAAMMDD_HHMMSS.log",
            "cyb_encrypt_20230621_011223_sql.log",
            "cyb_encrypt_20230621_011223_gpg.log",
            "cyb_encrypt_20230621_011223.log",
            "cy3_altera_dmstatus_cli_sem_fat_aberta_job_AAAAMMDD_HHMISS.log",
            "cy3_altera_dmstatus_cli_sem_fat_aberta_job_AAAAMMDD_HHMISS_sql.log",
            "cy3_altera_dmstatus_cli_sem_fat_aberta_utl_AAAAMMDD_HHMISS.log",
            "cy3_shell_kafka_producer017_DATA_HORA.log",
            "EVENTOS_PRODUCER_017_DATA_HORA.log",
            "cy3_shell_kafka_producer018_DATA_HORA.log",
            "EVENTOS_PRODUCER_018_DATA_HORA.log"
        );
        mascaras.forEach(item -> {
            log.info("ANTES: {}", item);
            log.info("DEPOIS: {}", FormatString.extrairMascara(item));
            System.out.println();
        });

        var teste = "proc_fluxo_quebra_acordo_AAAAMMDD_F{NUMEROFASE}_S{NUMEROSUBSET}.log";
        log.info("TESTE: {}", FormatString.abstrairVariavel(teste, "\\{.*?\\}"));
    }

    @Test
    public void testeRegexQuebraDelinhaComEspaco() throws JsonProcessingException {
//        String texto = "Olá\n        mundo!\n\n    Como\n\n        você está?";
        String texto = "GenerationTarget encountered exception accepting command : Error executing DDL \"\n" +
            " alter table ppw_exec_query \n" +
            " add query_nome varchar2(150 char) not null\" via JDBC Statement";

        // Remover quebra de linha + múltiplos espaços em branco
        String textoFormatado = texto.replaceAll("\\n\\s+", " ");

        System.out.println(textoFormatado);
    }

    @Test
    public void testarConversaoModelMapper_PipelineExecDTO() throws JsonProcessingException {
        val json = "{\"clienteId\":1,\"ambienteId\":1,\"pipeline\":{\"id\":141,\"name\":\"Carga Eventual de Produtos\",\"descricao\":\"\",\"clienteId\":1,\"jobs\":[\"cy3_ent_carga_ev_IE008.ksh\"]},\"relatorio\":{\"idProjeto\":\"IN1920\",\"nomeProjeto\":\"FENIX\",\"nomeAtividade\":\"Carga válida de produtos com validação mensal\",\"consideracoes\":\"\",\"testeTipo\":\"\"},\"userInfo\":{\"name\":\"João Aguiar\",\"empresa\":\"Peopleware\",\"papel\":\"DEV\",\"email\":\"joao.aguiar@ppware.com.br\",\"telefone\":\"\"},\"jobs\":[{\"id\":300,\"ordem\":0,\"argumentos\":\"20230621 mensal\",\"queries\":[]}]}\n";
        log.info("JSON:");
        log.info(json);
        val execDto = new ObjectMapper().readValue(json, PipelineExecDTO.class);
        log.info("Conversão ModelMapper:");
        log.info(execDto.toString());
    }

    @Test
    public void testandoComparacaoDeListas() {
        val tabelasSchedule = new ArrayList<String>();
        tabelasSchedule.add("TB_ESTACORDO_FATURAS_CONTAS");
        tabelasSchedule.add("TB_ESTACORDO_PARCELAS_CONTAS");
        tabelasSchedule.add("TB_ESTACORDO_RATEIO_PAGTOS");
        tabelasSchedule.add("EVENTOS_WEB");

        val tabelasConsultadas = new ArrayList<String>();
        tabelasConsultadas.add("DELQMST");
        tabelasConsultadas.add("EVENTOS_WEB");

        log.info("Tabelas na Schedule: {}", String.join(", ", tabelasSchedule));
        log.info("Tabelas Consultadas: {}", String.join(", ", tabelasConsultadas));

        val tabelasPendentes = new ArrayList<>(tabelasSchedule);
        tabelasPendentes.removeAll(tabelasConsultadas);

        val tabelasExtras = new ArrayList<>(tabelasConsultadas);
        tabelasExtras.removeAll(tabelasSchedule);

        log.info("Tabelas Pendentes de Consulta: {}", String.join(", ", tabelasPendentes));
        log.info("Tabelas Consultadas em Adicional: {}", String.join(", ", tabelasExtras));
    }

    @Test
    public void testeObterVersaoNoManifest() {
        val impVersion = getClass().getPackage().getImplementationVersion();
        log.info("Version: {}", impVersion);
    }

    @Test
    public void testeValidarQueryComDdl() {
        String sql = "SELECT INSERT * FROM DELQUDA_1_CLI WHERE ROWNUM <= 50";
        log.info("SQL: {}", sql);

        boolean valido = isSafeSelect(sql);
        log.info("Valido: {}", valido);

        sql = "SELECT * FROM DELQUDA_1_CLI WHERE ROWNUM <= 50";
        log.info("SQL: {}", sql);

        valido = isSafeSelect(sql);
        log.info("Valido: {}", valido);
    }

    @Test
    public void testeValidarExecucaoEmIdea() {
        val classPath = System.getProperty("java.class.path");
        log.info("ClassPath:");
        Arrays.stream(classPath.split(";")).forEach(cp -> log.info(" - {}", cp));

        val resultado = classPath.toLowerCase().contains("idea") || classPath.toLowerCase().contains("eclipse");
        log.info("Executando em IDEA: {}", resultado);
    }

    @Test
    public void testeObtendoRecursosInternos() throws IOException {
        val recurso = ResourceUtils.getFile("classpath:template/template.html");
        log.info("Recurso: {}", recurso);
    }

    @Test
    public void testeVerificarExecucaoEmIdea() {
        val idea = System.getProperty("idea.launcher.port");
        log.info("IDEA Marker: {}", idea);
    }

    @Test
    public void testeComandoListagemSftp() throws IOException {
        val cliente = "Vivo";
        val ftp = new AmbienteAcessoDTO(
            "10.129.164.206:22",
            "rcvry",
            "Ppw@1022");
        val arquivoPath = "/app/rcvry/cy3/log/EVENTOS_PRODUCER_017_*_*.log";
        val sftp = ConectorSftp.conectar(ftp.getConexao(), ftp.getUsuario(), ftp.getSenha());
        val listaArquivos = sftp.comando("ls -t " + arquivoPath + " | head -1").getConsoleLog();
        listaArquivos.forEach(log::info);
    }

    @Test
    public void dividirStringEmLista() {
        val tabelas = "TB_TEMP_CARGA_4P, TB_CONTROLE_SEQ, TB_ARQENTRADA";
        val listaTabelas = dividirValores(tabelas);
        listaTabelas.forEach(log::info);
    }

    @Test
    public void testarJobService_ComparacaoDeLogsMaisRecentes() {
        log.info("Teste na comparação dos logs mais recentes.");
        val dir = "temp/cy3_shell_kafka_producer017";
        val service = new JobService(null, null);

        val logsAntes = List.of(
            Path.of(dir, "cy3_shell_kafka_producer017_20230831_180958.log").toFile(),
            Path.of(dir, "EVENTOS_PRODUCER_017_20230831_180958.log").toFile()
        );
        val logAntesString = logsAntes.stream().map(File::getName).collect(Collectors.joining(", "));

        val logsDepois = List.of(
            Path.of(dir, "cy3_shell_kafka_producer_TESTE_20231020_094354.log").toFile(),
            Path.of(dir, "EVENTOS_PRODUCER_TESTE_20230831_180958.log").toFile()
        );
        val logDepoisString = logsDepois.stream().map(File::getName).collect(Collectors.joining(", "));

        log.info("logsAntes: '{}'", logAntesString);
        log.info("logsDepois: '{}'", logDepoisString);
        log.info("Comparando logs para anexar como evidência.");
    }

    @Test
    public void testandoLerProperties() throws IOException, SQLException {
        final Properties properties = lerProperties();
        properties.forEach((k, v) -> log.info("{}: {}", k, v));
        val url = properties.get("db.url").toString();
        val username = properties.get("db.username").toString();
        val password = properties.get("db.password").toString();

        log.info("Testando conexão.");
        val conn = DriverManager.getConnection(url, username, password);
        log.info("Conexão realizada com erro.");
    }

    private Properties lerProperties() throws IOException {
        //src/main/resources/db-config.properties
        try (InputStream input = new FileInputStream("src/main/resources/db-config.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        }
    }


//    @Test
//    public void testeObterMetaDadosViQuery() {
//        String url = "jdbc:oracle:thin:@10.129.226.159:1521/CCSSIDEV";
//        String user = "rcvry";
//        String password = "Vivo2015";
//        var ambiente = new AmbienteAcessoDTO();
//        ambiente.setConexao("10.129.226.159:1521/CCSSIDEV");
//        ambiente.setUsuario(user);
//        ambiente.setSenha(password);
//
//        try (var dao = new MasterOracleDAO(ambiente)) {
//            dao.getColumnsFromTable("DELQMST", "UDA1").forEach(
//                table -> log.info(table.toString())
//            );
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Test
    public void testeRegexExtrairNomeDasTabelas() {
        var query = """
                SELECT alias1.column1,   table_name2.  count(column2)
                FROM table_name1 alias1, table_name2 alias2
                INNER JOIN   table_name3
                ON alias1.fieldA = table_name3.fieldZ
                OR table_name1.fieldB = table_name3.fieldY
                WHERE NVL(fieldC, 0) >= 50.10
                AND fieldD EXIST (
                    SELECT column3
                    FROM table_name4
                    WHERE TO_DATE(fieldE, 'DD/MM/YY') = TO_DATE('userInputE1', 'DD/MM/YY')
                    OR fieldE <> ${variableE2}
                    ORDER BY column3 ASC
                )
                OR (fieldF=${variableF1})
                OR fieldF>='userInputF2'
                OR fieldF<='userInputF3'
                OR fieldF<>${variableF4}
                GROUP BY column1
                HAVING SUM(column2) > 100
                ORDER BY column1 DESC
                LIMIT 10
                OFFSET 5
                FETCH FIRST 5 ROWS ONLY
                UNION ALL
                SELECT column4, column5, column6
                FROM table_name5,
                     table_name6
                WHERE fieldG LIKE '%userInputG%'
                AND fieldH NOT LIKE ${variableH}
                AND (
                    UPPER(fieldI) IN (${variableI})
                    OR fieldJ NOT IN ('userInputJ')
                )
                ORDER BY column3 ASC
                MINUS
                SELECT *
                FROM (
                    SELECT column7, MAX(column8)
                    FROM table_name7 GROUP BY column7
                )
            """;
        var extraction = SqlSintaxe.analyse(query);
        var tables = extraction.tables();
        var columns = extraction.columns();
        var filters = extraction.filters();
        log.info("TABELAS: {}", String.join(", ", tables));
        log.info("COLUNAS: {}", String.join(", ", columns));
        log.info("FILTROS:");
        filters.forEach(filter -> log.info(" - {}", filter));

        var tabelasEsperadas = Set.of(
            "table_name1",
            "table_name2",
            "table_name3",
            "table_name4",
            "table_name5",
            "table_name6",
            "table_name7"
        );
        var colunasEsperadas = Set.of(
            "column1",
            "column2",
            "column3",
            "column4",
            "column5",
            "column6",
            "column7",
            "column8"
        );
        var filtrosEsperados = Set.of(
            new QueryFilter("fieldA", Set.of()),
            new QueryFilter("fieldB", Set.of()),
            new QueryFilter("fieldC", Set.of()),
            new QueryFilter("fieldD", Set.of()),
            new QueryFilter("fieldE", Set.of(
                new QueryVariable("variableE2", false))),
            new QueryFilter("fieldF", Set.of(
                new QueryVariable("variableF1", false),
                new QueryVariable("variableF4", false))),
            new QueryFilter("fieldG", Set.of()),
            new QueryFilter("fieldH", Set.of(
                new QueryVariable("variableH", false))),
            new QueryFilter("fieldI", Set.of(
                new QueryVariable("variableI", true))),
            new QueryFilter("fieldJ", Set.of()),
            new QueryFilter("fieldZ", Set.of()),
            new QueryFilter("fieldY", Set.of())
        );
        log.info("TABELAS ESPERADAS: {}", String.join(", ", tabelasEsperadas));
        log.info("COLUNAS ESPERADAS: {}", String.join(", ", colunasEsperadas));
        log.info("FILTROS ESPERADOS:");
        filtrosEsperados.forEach(filter -> log.info(" - {}", filter));

        Assertions.assertTrue(tables.containsAll(tabelasEsperadas), "Existe table pendente");
        Assertions.assertTrue(tabelasEsperadas.containsAll(tables), "Existe table a mais");

        Assertions.assertTrue(columns.containsAll(colunasEsperadas), "Existe coluna pendente");
        Assertions.assertTrue(colunasEsperadas.containsAll(columns), "Existe coluna a mais");

        Assertions.assertTrue(
            filters.parallelStream().allMatch(
                filter -> filtrosEsperados.parallelStream().anyMatch(esperado -> esperado.equals(filter))),
            "Existe filtro pendente"
        );
        Assertions.assertTrue(
            filtrosEsperados.parallelStream().allMatch(
                esperado -> filters.parallelStream().anyMatch(filter -> filter.equals(esperado))),
            "Existe filtro a mais"
        );

    }

    @Test
    public void testeExtrairQueriesDeArtefatosShell() throws IOException {
        var shellPath = "src/test/resources/cy3_shell_quebra_acordo.ksh";
        var shellFile = Path.of(shellPath).toFile();
        BashSintaxe.findQueriesInFile(shellFile);
    }


    @Test
    public void testeExtrairQueriesMetadadoBancoRemoto() {
        var queries = Set.of(
            "SELECT * FROM EVENTOS_WEB WHERE EVTYPE='EV_PAYMENTMODEL' AND EVACCT IN (${contratos}) AND TRUNC(EVDTSOLIC)=TRUNC(${data-evento-salvo}) ORDER BY EVID ASC",
            "SELECT * FROM TMP_ENTRADA_PAGTO WHERE PBACCT IN (${contratos}) ORDER BY PBID ASC"
        );
        var tables = new HashSet<String>();
        var columns = new HashSet<String>();
        var filters = new HashSet<QueryFilter>();

        log.info("QUERIES:");
        queries.stream()
            .peek(log::info)
            .parallel()
            .map(SqlSintaxe::analyse)
            .forEach(extraction -> {
                tables.addAll(extraction.tables());
                columns.addAll(extraction.columns());
                filters.addAll(extraction.filters());
            });
        log.info("TABELAS: {}", String.join(", ", tables));
        log.info("COLUNAS: {}", String.join(", ", columns));
        log.info("FILTROS:");
        filters.forEach(filter -> log.info(" - {}", filter));

        var tabelasEsperadas = Set.of(
            "EVENTOS_WEB",
            "TMP_ENTRADA_PAGTO"
        );
        var colunasEsperadas = Set.of();
        var filtrosEsperados = Set.of(
            new QueryFilter("EVTYPE", Set.of()),
            new QueryFilter("EVACCT", Set.of(
                new QueryVariable("contratos", true))),
            new QueryFilter("EVDTSOLIC", Set.of(
                new QueryVariable("data-evento-salvo", false))),
            new QueryFilter("PBACCT", Set.of(
                new QueryVariable("contratos", true)))
        );
        Assertions.assertTrue(tables.containsAll(tabelasEsperadas), "Existe table pendente");
        Assertions.assertTrue(tabelasEsperadas.containsAll(tables), "Existe table a mais");
        Assertions.assertTrue(columns.containsAll(colunasEsperadas), "Existe coluna pendente");
        Assertions.assertTrue(colunasEsperadas.containsAll(columns), "Existe coluna a mais");

        var ambiente = new AmbienteAcessoDTO(
            "10.129.164.205:1521:cyb3dev",
            "rcvry",
            "rcvry"
        );
        try(val masterDao = new MasterOracleDAO(ambiente)) {
            filters.parallelStream()
                .map(QueryFilter::column)
                .forEach(columns::add);
            var tablesDb = masterDao.extractInfoFromTables(tables, columns);
            log.info("METADADOS DO BANCO:");
            tablesDb.stream().forEach(table -> log.info(table.toString()));

            Assertions.assertTrue(
                tablesDb.parallelStream()
                    .map(DbTable::tabela)
                    .collect(Collectors.toSet())
                    .containsAll(tabelasEsperadas),
                "Existe table pendente após coleta dos metadados");

            Assertions.assertTrue(
                tablesDb.parallelStream()
                    .map(DbTable::getColumnsNames)
                    .flatMap(Set::parallelStream)
                    .collect(Collectors.toSet())
                    .containsAll(colunasEsperadas),
                "Existe coluna pendente após coleta dos metadados"
            );

            Assertions.assertTrue(
                tablesDb.parallelStream()
                    .map(DbTable::getColumnsNames)
                    .flatMap(Set::parallelStream)
                    .collect(Collectors.toSet())
                    .containsAll(
                        filtrosEsperados.parallelStream()
                            .map(QueryFilter::column)
                            .collect(Collectors.toSet())
                    ),
                "Existe coluna pendente após coleta dos metadados"
            );
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(getExceptionMainCause(e));
        }

    }

    @Test
    public void testeConverterTextoEmMapaDeFiltrosSql() throws SQLException {
        var sql = "" +
                "SELECT * " +
                "FROM TMP_ENTRADA_PAGTO " +
                "WHERE " +
                "   PBACCT IN (${contratos}) " +
                "   AND PBACCTG=${grupo} " +
                "   AND TRUNC(PBDTOCORR) = TRUNC(${ifxdate}) " +
                "ORDER BY PBID ASC";
//        var metadados = "contratos=string[], grupo=char";
        log.info(sql);
//        log.info(metadados);

        var novaSql = FormatString.substituirVariaveis(sql, "?");
        log.info("total: {}", novaSql);
    }

    @Test
    public void testeGetMavenProps() {
        try {
            //val jar = "C:\\Users\\joao.aguiar\\Workspace\\AGUIAR\\Det-Maker-Api\\target\\det-maker-api-v1.12.Beta.jar";
            val path = "C:\\Users\\joao.aguiar\\Workspace\\AGUIAR\\Det-Maker-Api\\target";
            val jarName = "det-maker-.*\\.jar";
            log.info("Path: {}", path);
            log.info("JarName: {}", jarName);

            val files = Optional.ofNullable(
                new File(path).listFiles((dir, name) -> name.matches(jarName)))
                .orElseThrow();
            val jar = Arrays.stream(files)
                .findFirst()
                .orElseThrow();

            JarFile jarFile = new JarFile(jar);
            Manifest manifest = jarFile.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            String version = attributes.getValue("Implementation-Version");
            log.info("Versão da aplicação: {}", version);
            jarFile.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testeDiferencaDeDatas() {
        val dataInicio = OffsetDateTime.now();
        val dataFim = dataInicio.plusHours(1);
        log.info("Data início: {}", dataInicio);
        log.info("Data fim: {}", dataFim);

        val duracao = Duration.between(dataInicio.toInstant(), dataFim.toInstant());
        log.info("Duração em segundos: {}", duracao.getSeconds());
        log.info("Duração em milésimos: {}", duracao.getSeconds() * 1000);
    }

//    @Test
//    public void testePreencherConfigQuerySqlComFiltroSql() {
//        String sql1 = "SELECT * FROM TMP_ENTRADA_PAGTO WHERE ${PBACCTG} = ${string} AND ${PBACCT} IN (${string[]}) ORDER BY PBID ASC";
//        String sql2 = "SELECT * FROM TMP_ENTRADA_PAGTO WHERE ${PBACCTG} = ${string} AND ${PBACCT}";
//        String sql3 = "SELECT * FROM TMP_ENTRADA_PAGTO WHERE ${PBACCTG} = ${string} AND ${PBACCT} IN (${string[]}) AND ${PBACCT} = ${string}";
//        val filtroSql1 = new FiltroSql("PBACCTG", "string");
//        filtroSql1.setValor("1");
//        val filtroSql2 = new FiltroSql("PBACCT", "string[]");
//        filtroSql2.setValor("215487621-BRM");
//        val listaFiltrosSql = List.of(filtroSql1, filtroSql2);
//
//        String resultado = FiltroSql.montarSql(sql1,listaFiltrosSql);
//        log.info(resultado);
//
//        resultado = FiltroSql.montarSql(sql2,listaFiltrosSql);
//        log.info(resultado);
//
//        resultado = FiltroSql.montarSql(sql3,listaFiltrosSql);
//        log.info(resultado);
//    }

}
