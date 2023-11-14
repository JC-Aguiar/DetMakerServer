package br.com.ppw.dma;

import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.ResultadoSql;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.LINHA_HIFENS;
import static br.com.ppw.dma.util.FormatString.dividirValores;

@Slf4j
public class BasicTest {

//    @Test
//    public void testeGerandoHtmlDetUnificado() throws IOException, URISyntaxException {
//        val dataHoraHoje = OffsetDateTime.now(RELOGIO);
//        val evidenciasDTO = List.of(
//            EvidenciaInfoDTO.builder()
//                .job("cy3_rem_notif.ksh")
//                .jobDescricao("Processa REMESSA NOTIFICACOES REGULATORIAS PARA SMARTBILL")
//                .data(dataHoraHoje.minusSeconds(32548))
//                .sucesso(true)
//                .ordem(0)
//                .argumentos("20230822")
//                .queries(List.of("SELECT * FROM SEQUENCIA s WHERE TPARQ='B023'"))
//                .tabelasPreJob(List.of("[#1] TPARQ=B023, SEQ=169"))
//                .tabelasPosJob(List.of("[#1] TPARQ=B023, SEQ=170"))
//                .logs(List.of("""
//                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
//                    Integer sit amet justo sit amet lacus faucibus viverra.
//
//                    Nunc nisi neque, volutpat vitae vulputate et, interdum et enim.
//                    Morbi ac quam tempus, consequat dolor et, tincidunt felis. Pellentesque pulvinar suscipit mauris sed dapibus. Nulla convallis leo eu eleifend euismod. Aliquam in ultricies libero. Nam tincidunt felis augue, eu mattis erat commodo at.
//                    Curabitur eleifend iaculis metus, vitae varius nisl dignissim accumsan.
//
//                    In ut nisi leo. Sed facilisis vitae libero in ultrices. Integer elit ante, efficitur sit amet aliquet eget, dapibus nec mauris. Donec luctus ut tellus vitae mollis. Cras tempus libero eu facilisis scelerisque. Nam in viverra tellus. Nulla auctor justo quis dolor rutrum, a faucibus enim fermentum. Nullam posuere leo tortor, at laoreet purus tristique ut. Proin at quam elit. Aliquam sed est suscipit, dapibus nibh eget, dapibus nunc. Praesent pellentesque libero eget lobortis euismod. Duis commodo eu ante et dapibus. Praesent mollis consectetur diam sed volutpat. Nunc non vehicula arcu.
//
//                    Vivamus at tempus elit. Suspendisse vitae sollicitudin magna.
//                    Sed varius diam sit amet porttitor pulvinar. Donec felis ipsum, finibus ac vulputate vel, fermentum vitae diam. Duis aliquam pharetra orci et sagittis. Proin tincidunt ex a laoreet dictum. Nulla non ante bibendum, molestie ante in, hendrerit arcu.
//
//                    Praesent pellentesque, lacus sed consectetur convallis, mi est facilisis eros, id eleifend leo ipsum nec velit. Duis finibus erat at velit porta sollicitudin.
//                    """))
//                .logsNome(List.of("cyb_encrypt_20230621_011223.log"))
//                .build()
//        );
//        val relatorioDto = RelatorioHistoricoDTO.builder()
//            .nomeProjeto("Teste Unitário DET-MAKER")
//            .nomeAtividade("testeGerandoHtmlDetUnificado")
//            .configuracao("Nenhuma")
//            .dataInicio(dataHoraHoje.minusMinutes(3))
//            .dataFim(dataHoraHoje)
//            .evidencias(evidenciasDTO)
//            .sucesso(false)
//            .build();
//
//        val pipelineRelatorio = new PipelineRelatorioDTO(
//            "Teste 01",
//            "Tentando criar documento DET com HTML, CSS e JS unificados",
//            relatorioDto);
//
//        val dbConfig = new DatabaseConfig();
//        dbConfig.setDbAmbiente("DEV");
//        dbConfig.setDbSistema("Vivo Cyber 3");
//
//        val usersInfo = List.of(UserInfoDTO.builder()
//            .nome("João Aguiar")
//            .papel("DEV")
//            .empresa("Peopleware")
//            .email("joao.aguiar@ppware.com.br")
//            .telefone("(13) 988465656")
//            .build());
//
//        DetHtml.gerarNovoDet(pipelineRelatorio, dbConfig, usersInfo);
//    }

    @Test
    public void testeDeExtracaoStringDoBanco() {
        val fields = List.of(
            "EVID", "EVACCTG", "EVACCT", "EVTYPE", "EVDTREG", "EVDTSOLIC",
            "EVDTPROC", "EVEXPDESC", "EVSTATUS", "EVOBJ");
        ResultadoSql resultadoSql = new ResultadoSql();
        resultadoSql.setCampos(fields);
        resultadoSql.setFiltros("SELECT EVID, EVACCTG, EVACCT, EVTYPE, EVDTREG, EVDTSOLIC, EVDTPROC, EVEXPDESC, EVSTATUS, EVOBJ FROM EVENTOS_WEB WHERE EVDTSOLIC >= TO_DATE('10-10-2023', 'DD-MM-YYYY') AND  ROWNUM <= 50");
        resultadoSql.setTabela("EVENTOS_WEB");

        val extracao = List.of(
            new Object[] {8306, "1", "129902584-BRM", "EV_PRODUCTUSAGEMODEL", "2023-10-30 21:00:00.0", "2023-11-01 09:41:09.866", "2023-11-01 09:49:50.177", null, 9.0, null},
            new Object[] {8307, "1", "129902584-BRM", "EV_PRODUCTUSAGEMODEL", "2023-10-30 21:00:00.0", "2023-11-01 09:41:14.329", "2023-11-01 09:49:50.414", null, 9.0, null},
            new Object[] {8383, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:22:08.072391", "2023-11-09 13:22:08.072391", "5072456-2", 0.0, null},
            new Object[] {8303, "1", "129484935-BRM", "EV_PRODUCTUSAGEMODEL", "2023-10-29 21:00:00.0", "2023-10-30 18:18:36.919", null, "Evento ID 8303 sem contrato na DELQMST", 0.0, null},
            new Object[] {8384, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:22:08.141545", "2023-11-09 13:22:08.141545", "5072517-2", 0.0, null},
            new Object[] {8385, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:22:08.152563", "2023-11-09 13:22:08.152563", "59072572-1", 9.0, null},
            new Object[] {8331, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:03:18.087908", "2023-11-06 18:03:18.087908", "59072572-1", 9.0, null},
            new Object[] {8332, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:16:11.878461", "2023-11-06 18:16:11.878461", "5072456-2", 0.0, null},
            new Object[] {8329, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:03:18.052309", "2023-11-06 18:03:18.052309", "5072456-2", 0.0, null},
            new Object[] {8330, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:03:18.079681", "2023-11-06 18:03:18.079681", "5072517-2", 0.0, null},
            new Object[] {8333, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:16:11.898724", "2023-11-06 18:16:11.898724", "5072517-2", 0.0, null},
            new Object[] {8334, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:16:11.906207", "2023-11-06 18:16:11.906207", "59072572-1", 9.0, null},
            new Object[] {8335, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:32:36.640344", "2023-11-06 18:32:36.640344", "5072456-2", 0.0, null},
            new Object[] {8336, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:32:36.665165", "2023-11-06 18:32:36.665165", "5072517-2", 0.0, null},
            new Object[] {8337, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:32:36.672289", "2023-11-06 18:32:36.672289", "59072572-1", 9.0, null},
            new Object[] {8338, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:43:37.527024", "2023-11-06 18:43:37.527024", "5072456-2", 0.0, null},
            new Object[] {8339, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:43:37.55551", "2023-11-06 18:43:37.55551", "5072517-2", 0.0, null},
            new Object[] {8340, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:43:37.564004", "2023-11-06 18:43:37.564004", "59072572-1", 9.0, null},
            new Object[] {8341, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:18:28.87082", "2023-11-06 19:18:28.87082", "5072456-2", 0.0, null},
            new Object[] {8342, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:18:28.905671", "2023-11-06 19:18:28.905671", "5072517-2", 0.0, null},
            new Object[] {8343, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:18:28.911902", "2023-11-06 19:18:28.911902", "59072572-1", 9.0, null},
            new Object[] {8344, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:25:22.132048", "2023-11-06 19:25:22.132048", "5072456-2", 0.0, null},
            new Object[] {8345, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:25:22.180125", "2023-11-06 19:25:22.180125", "5072517-2", 0.0, null},
            new Object[] {8346, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:25:22.188334", "2023-11-06 19:25:22.188334", "59072572-1", 9.0, null},
            new Object[] {8350, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 12:47:30.778031", "2023-11-07 12:47:30.778031", "5072456-2", 0.0, null},
            new Object[] {8351, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 12:47:30.83287", "2023-11-07 12:47:30.83287", "5072517-2", 0.0, null},
            new Object[] {8352, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 12:47:30.85859", "2023-11-07 12:47:30.85859", "59072572-1", 9.0, null},
            new Object[] {8356, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:40:23.423202", "2023-11-07 13:40:23.423202", "5072456-2", 0.0, null},
            new Object[] {8357, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:40:23.47023", "2023-11-07 13:40:23.47023", "5072517-2", 0.0, null},
            new Object[] {8358, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:40:23.484367", "2023-11-07 13:40:23.484367", "59072572-1", 9.0, null},
            new Object[] {8359, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:44:06.409415", "2023-11-07 13:44:06.409415", "5072456-2", 0.0, null},
            new Object[] {8360, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:44:06.440669", "2023-11-07 13:44:06.440669", "5072517-2", 0.0, null},
            new Object[] {8361, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:44:06.45436", "2023-11-07 13:44:06.45436", "59072572-1", 9.0, null},
            new Object[] {8362, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:48:10.706153", "2023-11-07 13:48:10.706153", "5072456-2", 0.0, null},
            new Object[] {8363, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:48:10.730559", "2023-11-07 13:48:10.730559", "5072517-2", 0.0, null},
            new Object[] {8364, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:48:10.741156", "2023-11-07 13:48:10.741156", "59072572-1", 9.0, null},
            new Object[] {8365, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:58:14.445982", "2023-11-07 13:58:14.445982", "5072456-2", 0.0, null},
            new Object[] {8366, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:58:14.475001", "2023-11-07 13:58:14.475001", "5072517-2", 0.0, null},
            new Object[] {8367, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:58:14.482021", "2023-11-07 13:58:14.482021", "59072572-1", 9.0, null},
            new Object[] {8386, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:26:15.791464", "2023-11-09 13:26:15.791464", "5072456-2", 0.0, null},
            new Object[] {8387, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:26:15.881686", "2023-11-09 13:26:15.881686", "5072517-2", 0.0, null},
            new Object[] {8388, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:26:15.911694", "2023-11-09 13:26:15.911694", "59072572-1", 9.0, null},
            new Object[] {8389, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:51:17.521517", "2023-11-09 13:51:17.521517", "5072456-2", 0.0, null},
            new Object[] {8390, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:51:17.543858", "2023-11-09 13:51:17.543858", "5072517-2", 0.0, null},
            new Object[] {8391, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:51:17.551863", "2023-11-09 13:51:17.551863", "59072572-1", 9.0, null},
            new Object[] {8371, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:07:43.94025", "2023-11-07 18:07:43.94025", "5072456-2", 0.0, null},
            new Object[] {8372, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:07:43.980774", "2023-11-07 18:07:43.980774", "5072517-2", 0.0, null},
            new Object[] {8373, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:07:43.988481", "2023-11-07 18:07:43.988481", "59072572-1", 9.0, null},
            new Object[] {8374, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:18:21.387146", "2023-11-07 18:18:21.387146", "5072456-2", 0.0, null},
            new Object[] {8375, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:18:21.412563", "2023-11-07 18:18:21.412563", "5072517-2", 0.0, null}
        );
        extracao.forEach(obj -> {
            Object[] elemento = (Object[]) obj;
            log.info("{}", Arrays.toString(elemento));
            val resultSet = new HashMap<String, Object>();

            for(int i = 0; i < fields.size(); i++) {
                resultSet.put(fields.get(i), elemento[i]);
            }
            resultSet.forEach((k, v) -> log.info(" - Coletado: '{}' = {}", k, v));
            resultadoSql.addResultado(resultSet);
        });
        //Exibindo resultados
        log.info(LINHA_HIFENS);
        resultadoSql.fecharConsultaPreJob();
        log.info("ResultadoSql.toString:");
        resultadoSql.getResultadoPreJob().forEach(obj -> log.info(String.valueOf(obj)));
        log.info(LINHA_HIFENS);
        log.info("ResultadoSql.getResumo:");
        log.info("\n{}", resultadoSql.getResumoPreJob());
    }

    @Test
    public void testeDeColetaGenericaAoBanco() {
        val comandoSql = new ComandoSql();
        comandoSql.setFiltros("SELECT * FROM EVENTOS_WEB ew WHERE EVTYPE='EV_BOLETO_CYBER_HUBPGTO' ORDER BY EVID");
        comandoSql.setTabela("EVENTOS_WEB");
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


}
