package br.com.ppw.dma;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ConfigQueryVar;
import br.com.ppw.dma.configQuery.FiltroSql;
import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.pipeline.PipelineExecDTO;
import br.com.ppw.dma.util.FormatString;
import br.com.ppw.dma.util.SqlUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lalyos.jfiglet.FigletFont;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.dividirValores;

@Slf4j
public class BasicTest {

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
    public void testeApacheMinaSsh() throws IOException {
        var mensagem = sshCommandTest(
            "rcvry",
            "rcvry",
            "10.129.226.157:22",
            15,
            "pwd");
        log.info(mensagem);

//        var propsIguais = vivo1Props.keySet()
//            .stream()
//            .filter(vivo3Props::containsKey)
//            .toList();
//        log.info("PROPS IGUAIS VIVO1");
//        vivo1Props.keySet()
//            .stream()
//            .filter(propsIguais::contains)
//            .forEach(key -> log.info("{}: {}", key, vivo1Props.get(key)));
//        log.info("PROPS IGUAIS VIVO3");
//        vivo3Props.keySet()
//            .stream()
//            .filter(propsIguais::contains)
//            .forEach(key -> log.info("{}: {}", key, vivo3Props.get(key)));
    }

    public static String sshCommandTest(
        String username,
        String password,
        String hostFull,
        long defaultTimeoutSeconds,
        String command)
        throws IOException {
        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        String host = hostFull.split(":")[0];
        int port = Integer.parseInt(hostFull.split(":")[1]);

        try (ClientSession session = client.connect(username, host, port)
            .verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {
            session.addPasswordIdentity(password);
            session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createChannel(Channel.CHANNEL_SHELL)) {
                //---------------------------------------------------------------------
                channel.setOut(responseStream);
                channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                try (OutputStream pipedIn = channel.getInvertedIn()) {
                    pipedIn.write(command.getBytes());
                    pipedIn.flush();
                }
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                    TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds));
                String responseString = new String(responseStream.toByteArray());
                return responseString;
            }
        }
        finally {
            client.stop();
        }
    }

    @Test
    public void testeBannerFormatado() throws IOException {
        String[] banner = FigletFont.convertOneLine("DET-MAKER").split("\n");
        System.out.println();
        Arrays.stream(banner)
            .filter(linha -> !linha.trim().isEmpty())
            .forEach(System.out::println);
        System.out.println(":: Det-Maker ::                 (v20240314-DEV)");
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
        val json = "{\"clienteId\":1,\"ambienteId\":1,\"pipeline\":{\"id\":141,\"nome\":\"Carga Eventual de Produtos\",\"descricao\":\"\",\"clienteId\":1,\"jobs\":[\"cy3_ent_carga_ev_IE008.ksh\"]},\"relatorio\":{\"idProjeto\":\"IN1920\",\"nomeProjeto\":\"FENIX\",\"nomeAtividade\":\"Carga válida de produtos com validação mensal\",\"consideracoes\":\"\",\"testeTipo\":\"\"},\"userInfo\":{\"nome\":\"João Aguiar\",\"empresa\":\"Peopleware\",\"papel\":\"DEV\",\"email\":\"joao.aguiar@ppware.com.br\",\"telefone\":\"\"},\"jobs\":[{\"id\":300,\"ordem\":0,\"argumentos\":\"20230621 mensal\",\"queries\":[]}]}\n";
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

        boolean valido = SqlUtils.isSafeQuery(sql);
        log.info("Valido: {}", valido);

        sql = "SELECT * FROM DELQUDA_1_CLI WHERE ROWNUM <= 50";
        log.info("SQL: {}", sql);

        valido = SqlUtils.isSafeQuery(sql);
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
        log.info("Conexão realizada com sucesso.");
    }

    private Properties lerProperties() throws IOException {
        //src/main/resources/db-config.properties
        try (InputStream input = new FileInputStream("src/main/resources/db-config.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        }
    }

    @Test
    public void testeCriarValidarComandoSqlEmConfigQueryVars() throws SQLException {
        var ambiente = new Ambiente();
        ambiente.setConexaoBanco("10.129.164.205:1521:cyb3dev");
        ambiente.setUsuarioBanco("rcvry");
        ambiente.setSenhaBanco("rcvry");
        var banco = AmbienteAcessoDTO.banco(ambiente);
        log.info(banco.toString());

        var filtro1 = new FiltroSql();
        filtro1.setTabela("EVENTOS_WEB");
        filtro1.setColuna("EVDTPROC");
        filtro1.setTipo("UNSET");
        filtro1.setIndex(0);
        filtro1.setArray(false);
        filtro1.setVariavel("data-evento-processo");
        var filtro2 = new FiltroSql();
        filtro2.setTabela("EVENTOS_WEB");
        filtro2.setColuna("EVACCT");
        filtro2.setTipo("UNSET");
        filtro2.setIndex(1);
        filtro2.setArray(true);
        filtro2.setVariavel("contrato");
        var comando = new ComandoSql();
        comando.setJobId(173L);
        comando.setNome("Eventos de Pagamento BRM");
        comando.setSql(
            "SELECT * FROM EVENTOS_WEB WHERE EVTYPE='EV_BOLETO_CYBER_HUBPGTO' AND TRUNC" +
                "(EVDTPROC)=TRUNC(${data-evento-processo}) AND EVACCT IN (${contrato}) ORDER BY EVID ASC");
        comando.setFiltros(List.of(filtro1, filtro2));
        log.info(comando.toString());

        try(val masterDao = new MasterOracleDAO(banco)) {
            log.info("Obtendo metadados das variáveis.");
            comando.mapFiltrosPorTabela()
                .forEach(masterDao::findAndSetColumnInfo);

            log.info("Convertendo para ConfigQueryVars.");
            var queryVars = comando.getFiltros()
                .stream()
                .map(ConfigQueryVar::new)
                .peek(vars -> log.info(vars.toString()))
                .toList();

            log.info("Criando valores aleatórios para testar as variáveis da query.");
            var mapaVariavelValor = ConfigQueryVar.mapaDasVariaveis(queryVars);
            log.info("Variáveis: {}", mapaVariavelValor);

            var sql = FormatString.substituirVariaveis(comando.getSql(), mapaVariavelValor);
            masterDao.validadeQuery(sql);
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
