package br.com.ppw.dma;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.carga.PadraoCargaProdutoGLW;
import br.com.ppw.dma.configQuery.FiltroSql;
import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.pipeline.PipelineExecDTO;
import br.com.ppw.dma.util.SqlUtils;
import br.com.ppware.NumeroAleatorio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lalyos.jfiglet.FigletFont;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.util.FormatString.dividirValores;

@Slf4j
public class BasicTest {

    public static final List<String> REGISTROS_CRIADOS = new ArrayList<>(Arrays.asList(
        "1#182015159141-MOK 54134418638 CTA-V0001NNM8Q-013",
        "1#694918356017-MOK 56237660955 CTA-V0001NNM06-013",
        "1#976705153873-MOK 12600793547 4130942898",
        "1#696370720150-MOK 54999655776 11975981679",
        "1#928461850636-MOK 16959806304 11999223054",
        "1#559548337639-MOK 54686031879 CTA-V0001NNMJ5-013",
        "1#256974321866-MOK 77558568915 11975981679",
        "1#688307547620-MOK 38612421325 CTA-V0001NNNT3-013",
        "1#314839724968-MOK 70582162656 CTA-V0001NNNXT-013",
        "1#956306269061-MOK 84310248037 4130942898",
        "1#943784923008-MOK 47195163276 TV-CTA-V0001NNLZJ-050",
        "1#260154707676-MOK 70538911843 4131071950",
        "1#492985891497-MOK 83463161450 11997027002",
        "1#218446172137-MOK 74234814677 TV-CTA-V0001NNMJ6-050",
        "1#770555023115-MOK 15032024971 CTA-V0001NNM8Q-013",
        "1#130181368621-MOK 85119911281 CTA-V0001NNNXT-013",
        "1#866839209261-MOK 35764687088 4131071124",
        "1#863348335247-MOK 37666599538 TV-CTA-V0001NNNXU-050",
        "1#696974437896-MOK 00866402749 TV-CTA-V0001NNNT2-050",
        "1#732911155025-MOK 39138172502 11999223054",
        "1#813548536808-MOK 90089015443 CTA-V0001NNLZI-013",
        "1#230222099163-MOK 46969503800 11997027002",
        "1#038047925229-MOK 01478596188 TV-CTA-V0001NNLZJ-050",
        "1#286953494180-MOK 76013792096 CTA-V0001NNM8Q-013",
        "1#644575994351-MOK 58463097538 TV-CTA-V0001NNM8R-050",
        "1#221698926753-MOK 99346277181 CTA-V0001NNMJ5-013",
        "1#223386059243-MOK 80121065446 CTA-V0001NNNT3-013",
        "1#995244920118-MOK 03241919500 11975981679",
        "1#200330377032-MOK 18114125032 CTA-V0001NNNXT-013",
        "1#298844413100-MOK 02053426687 TV-CTA-V0001NNM8R-050",
        "1#684636670384-MOK 06971572978 11999504335",
        "1#609299126914-MOK 63163589906 TV-CTA-V0001NNNXU-050",
        "1#973424098243-MOK 12921123379 11999819205",
        "1#563340844752-MOK 10496498785 11999819205",
        "1#538571498123-MOK 28513439415 11997027002",
        "1#313958167115-MOK 38324365503 TV-CTA-V0001NNLZJ-050",
        "1#885275354450-MOK 80674589665 TV-CTA-V0001NNLZJ-050",
        "1#380145520461-MOK 20186150924 11999504335",
        "1#150254761819-MOK 03091995469 4131071124",
        "1#384652788334-MOK 31478273902 CTA-V0001NNM8Q-013",
        "1#408177840572-MOK 41380370105 11999223054",
        "1#323258300350-MOK 99139830813 TV-CTA-V0001NNMJ6-050",
        "1#329652631291-MOK 50914613639 11997027002",
        "1#536625845853-MOK 04961375165 TV-CTA-V0001NNMJ6-050",
        "1#708874218165-MOK 66725164524 Servicos Digitais",
        "1#885086694357-MOK 11218734249 11999819205",
        "1#859574832867-MOK 40140780775 TV-CTA-V0001NNNT2-050",
        "1#792953750421-MOK 98891886448 4131071124",
        "1#851505628388-MOK 83549814163 CTA-V0001NNNXT-013",
        "1#177380661797-MOK 85626308996 CTA-V0001NNM8Q-013",
        "1#020851838389-MOK 66145580283 TV-CTA-V0001NNMJ6-050",
        "1#749707947489-MOK 37065679851 11999504335",
        "1#365396992524-MOK 29437691194 TV-CTA-V0001NNLZJ-050",
        "1#376445081128-MOK 52613900538 CTA-V0001NNLZI-013",
        "1#086985738716-MOK 22311168271 TV-CTA-V0001NNLZJ-050",
        "1#838249370839-MOK 94230665972 11999227503",
        "1#128947052590-MOK 19170628500 TV-CTA-V0001NNM07-050",
        "1#695851017399-MOK 33229116219 11999223054",
        "1#078461470131-MOK 23322045532 4130942898",
        "1#948317769292-MOK 96971467167 CTA-V0001NNNXT-013",
        "1#604226335398-MOK 37606648414 TV-CTA-V0001NNLZJ-050",
        "1#529283662160-MOK 99673794635 11975981679",
        "1#141271969810-MOK 98622778872 CTA-V0001NNM06-013",
        "1#058516491705-MOK 23162373582 4131071950"
    ));

    public static final List<String> ID_PRODUTO = List.of(
        "Servicos Digitais",
        "CTA-V0001NNLZI-013",
        "CTA-V0001NNM06-013",
        "TV-CTA-V0001NNLZJ-050",
        "TV-CTA-V0001NNM07-050",
        "11975981679",
        "11999504335",
        "11999819205",
        "11999227503",
        "CTA-V0001NNM8Q-013",
        "TV-CTA-V0001NNM8R-050",
        "4131071950",
        "11997027002",
        "CTA-V0001NNMJ5-013",
        "TV-CTA-V0001NNMJ6-050",
        "4131071124",
        "CTA-V0001NNNT3-013",
        "TV-CTA-V0001NNNT2-050",
        "11999223054",
        "4130942898",
        "CTA-V0001NNNXT-013",
        "TV-CTA-V0001NNNXU-050"
    );

    public static final List<String> TIPO_PRODUTO = List.of(
        "TV",
        "Linha Fixa",
        "Linha Móvel",
        "Banda Larga"
    );

    public static final DateTimeFormatter DATA_FORMATO = DateTimeFormatter.ofPattern("ddMMyyyy");

    public static String getidProduto() {
        return ID_PRODUTO.get(
            new Random().nextInt(0, ID_PRODUTO.size())
        );
    }

    public static String getTipoProduto() {
        return TIPO_PRODUTO.get(
            new Random().nextInt(0, TIPO_PRODUTO.size())
        );
    }

    public static final String GET_TIPO_STATUS = NumeroAleatorio
        .novoInteger(0, 2) > 0 ? "Ativo" : "Retirado";


    public static String preencherEspacosCampoCarga(@NonNull String texto, int tamanhoMax) {
        val builder = new StringBuilder(texto);
        if(tamanhoMax - builder.length() > 0)
            builder.append(" ".repeat(tamanhoMax - builder.length()));
        return builder.toString();
    }

    private static String criarRegistroCargaGLW() {
        var builder = new StringBuilder();
        Arrays.stream(PadraoCargaProdutoGLW.values())
            .map(PadraoCargaProdutoGLW::gerarCampo)
            .forEach(builder::append);
        var conteudo = builder.toString();
        var chave = Arrays.stream(conteudo.substring(0, 82).split(" "))
            .filter(txt -> !txt.trim().isEmpty())
            .collect(Collectors.joining(" "));

        if(REGISTROS_CRIADOS.contains(chave)) return criarRegistroCargaGLW();
        REGISTROS_CRIADOS.add(chave);
        return conteudo;
    }

    @Test
    public void shell_vivoProdutos_identificarPadrao() {
        Stream.of(new String[420])
            .map(obj -> criarRegistroCargaGLW())
            .forEach(System.out::println);
    }

    @Test
    public void shell_vivoProdutos_geradorDeCarga() {

    }

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
//        val pipelineRelatorio = new DetDTO(
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

//    @Test
//    public void testeDeExtracaoStringDoBanco() {
//        val fields = List.of(
//            "EVID", "EVACCTG", "EVACCT", "EVTYPE", "EVDTREG", "EVDTSOLIC",
//            "EVDTPROC", "EVEXPDESC", "EVSTATUS", "EVOBJ");
//        ResultadoSql resultadoSql = new ResultadoSql();
//        resultadoSql.setCampos(fields);
//        resultadoSql.setFiltros("SELECT EVID, EVACCTG, EVACCT, EVTYPE, EVDTREG, EVDTSOLIC, EVDTPROC, EVEXPDESC, EVSTATUS, EVOBJ FROM EVENTOS_WEB WHERE EVDTSOLIC >= TO_DATE('10-10-2023', 'DD-MM-YYYY') AND  ROWNUM <= 50");
//        resultadoSql.setTabela("EVENTOS_WEB");
//
//        val extracao = List.of(
//            new Object[] {8306, "1", "129902584-BRM", "EV_PRODUCTUSAGEMODEL", "2023-10-30 21:00:00.0", "2023-11-01 09:41:09.866", "2023-11-01 09:49:50.177", null, 9.0, null},
//            new Object[] {8307, "1", "129902584-BRM", "EV_PRODUCTUSAGEMODEL", "2023-10-30 21:00:00.0", "2023-11-01 09:41:14.329", "2023-11-01 09:49:50.414", null, 9.0, null},
//            new Object[] {8383, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:22:08.072391", "2023-11-09 13:22:08.072391", "5072456-2", 0.0, null},
//            new Object[] {8303, "1", "129484935-BRM", "EV_PRODUCTUSAGEMODEL", "2023-10-29 21:00:00.0", "2023-10-30 18:18:36.919", null, "Evento ID 8303 sem contrato na DELQMST", 0.0, null},
//            new Object[] {8384, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:22:08.141545", "2023-11-09 13:22:08.141545", "5072517-2", 0.0, null},
//            new Object[] {8385, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:22:08.152563", "2023-11-09 13:22:08.152563", "59072572-1", 9.0, null},
//            new Object[] {8331, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:03:18.087908", "2023-11-06 18:03:18.087908", "59072572-1", 9.0, null},
//            new Object[] {8332, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:16:11.878461", "2023-11-06 18:16:11.878461", "5072456-2", 0.0, null},
//            new Object[] {8329, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:03:18.052309", "2023-11-06 18:03:18.052309", "5072456-2", 0.0, null},
//            new Object[] {8330, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:03:18.079681", "2023-11-06 18:03:18.079681", "5072517-2", 0.0, null},
//            new Object[] {8333, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:16:11.898724", "2023-11-06 18:16:11.898724", "5072517-2", 0.0, null},
//            new Object[] {8334, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:16:11.906207", "2023-11-06 18:16:11.906207", "59072572-1", 9.0, null},
//            new Object[] {8335, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:32:36.640344", "2023-11-06 18:32:36.640344", "5072456-2", 0.0, null},
//            new Object[] {8336, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:32:36.665165", "2023-11-06 18:32:36.665165", "5072517-2", 0.0, null},
//            new Object[] {8337, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:32:36.672289", "2023-11-06 18:32:36.672289", "59072572-1", 9.0, null},
//            new Object[] {8338, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:43:37.527024", "2023-11-06 18:43:37.527024", "5072456-2", 0.0, null},
//            new Object[] {8339, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:43:37.55551", "2023-11-06 18:43:37.55551", "5072517-2", 0.0, null},
//            new Object[] {8340, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 18:43:37.564004", "2023-11-06 18:43:37.564004", "59072572-1", 9.0, null},
//            new Object[] {8341, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:18:28.87082", "2023-11-06 19:18:28.87082", "5072456-2", 0.0, null},
//            new Object[] {8342, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:18:28.905671", "2023-11-06 19:18:28.905671", "5072517-2", 0.0, null},
//            new Object[] {8343, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:18:28.911902", "2023-11-06 19:18:28.911902", "59072572-1", 9.0, null},
//            new Object[] {8344, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:25:22.132048", "2023-11-06 19:25:22.132048", "5072456-2", 0.0, null},
//            new Object[] {8345, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:25:22.180125", "2023-11-06 19:25:22.180125", "5072517-2", 0.0, null},
//            new Object[] {8346, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-06 19:25:22.188334", "2023-11-06 19:25:22.188334", "59072572-1", 9.0, null},
//            new Object[] {8350, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 12:47:30.778031", "2023-11-07 12:47:30.778031", "5072456-2", 0.0, null},
//            new Object[] {8351, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 12:47:30.83287", "2023-11-07 12:47:30.83287", "5072517-2", 0.0, null},
//            new Object[] {8352, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 12:47:30.85859", "2023-11-07 12:47:30.85859", "59072572-1", 9.0, null},
//            new Object[] {8356, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:40:23.423202", "2023-11-07 13:40:23.423202", "5072456-2", 0.0, null},
//            new Object[] {8357, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:40:23.47023", "2023-11-07 13:40:23.47023", "5072517-2", 0.0, null},
//            new Object[] {8358, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:40:23.484367", "2023-11-07 13:40:23.484367", "59072572-1", 9.0, null},
//            new Object[] {8359, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:44:06.409415", "2023-11-07 13:44:06.409415", "5072456-2", 0.0, null},
//            new Object[] {8360, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:44:06.440669", "2023-11-07 13:44:06.440669", "5072517-2", 0.0, null},
//            new Object[] {8361, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:44:06.45436", "2023-11-07 13:44:06.45436", "59072572-1", 9.0, null},
//            new Object[] {8362, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:48:10.706153", "2023-11-07 13:48:10.706153", "5072456-2", 0.0, null},
//            new Object[] {8363, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:48:10.730559", "2023-11-07 13:48:10.730559", "5072517-2", 0.0, null},
//            new Object[] {8364, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:48:10.741156", "2023-11-07 13:48:10.741156", "59072572-1", 9.0, null},
//            new Object[] {8365, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:58:14.445982", "2023-11-07 13:58:14.445982", "5072456-2", 0.0, null},
//            new Object[] {8366, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:58:14.475001", "2023-11-07 13:58:14.475001", "5072517-2", 0.0, null},
//            new Object[] {8367, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 13:58:14.482021", "2023-11-07 13:58:14.482021", "59072572-1", 9.0, null},
//            new Object[] {8386, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:26:15.791464", "2023-11-09 13:26:15.791464", "5072456-2", 0.0, null},
//            new Object[] {8387, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:26:15.881686", "2023-11-09 13:26:15.881686", "5072517-2", 0.0, null},
//            new Object[] {8388, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:26:15.911694", "2023-11-09 13:26:15.911694", "59072572-1", 9.0, null},
//            new Object[] {8389, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:51:17.521517", "2023-11-09 13:51:17.521517", "5072456-2", 0.0, null},
//            new Object[] {8390, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:51:17.543858", "2023-11-09 13:51:17.543858", "5072517-2", 0.0, null},
//            new Object[] {8391, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-09 13:51:17.551863", "2023-11-09 13:51:17.551863", "59072572-1", 9.0, null},
//            new Object[] {8371, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:07:43.94025", "2023-11-07 18:07:43.94025", "5072456-2", 0.0, null},
//            new Object[] {8372, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:07:43.980774", "2023-11-07 18:07:43.980774", "5072517-2", 0.0, null},
//            new Object[] {8373, "*", "8789855099-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:07:43.988481", "2023-11-07 18:07:43.988481", "59072572-1", 9.0, null},
//            new Object[] {8374, "*", "4000100003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:18:21.387146", "2023-11-07 18:18:21.387146", "5072456-2", 0.0, null},
//            new Object[] {8375, "*", "2711810003-BRM", "EV_BOLETO_CYBER_HUBPGTO", "2023-08-22 00:00:00.0", "2023-11-07 18:18:21.412563", "2023-11-07 18:18:21.412563", "5072517-2", 0.0, null}
//        );
//        extracao.forEach(obj -> {
//            Object[] elemento = (Object[]) obj;
//            log.info("{}", Arrays.toString(elemento));
//            val resultSet = new HashMap<String, Object>();
//
//            for(int i = 0; i < fields.size(); i++) {
//                resultSet.put(fields.get(i), elemento[i]);
//            }
//            resultSet.forEach((k, v) -> log.info(" - Coletado: '{}' = {}", k, v));
//            resultadoSql.addResultado(resultSet);
//        });
//        //Exibindo resultados
//        log.info(LINHA_HIFENS);
//        resultadoSql.fecharConsultaPreJob();
//        log.info("ResultadoSql.toString:");
//        resultadoSql.getResultadoPreJob().forEach(obj -> log.info(String.valueOf(obj)));
//        log.info(LINHA_HIFENS);
//        log.info("ResultadoSql.getResumo:");
//        log.info("\n{}", resultadoSql.getResumoPreJob());
//    }

//    @Test
//    public void testeDeColetaGenericaAoBanco() {
//        val comandoSql = new ComandoSql();
//        comandoSql.setFiltros("SELECT * FROM EVENTOS_WEB ew WHERE EVTYPE='EV_BOLETO_CYBER_HUBPGTO' ORDER BY EVID");
//        comandoSql.setTabela("EVENTOS_WEB");
//    }

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
    public void testeConverterTextoEmMapaDeFiltrosSql() {
        val filtro = "EVTYPE='EV_PAYMENTMODEL' AND ${EVACCT} IN (${string[]}) AND ${EVACCTG} = ${char}";
        log.info(filtro);

        FiltroSql.identificar(filtro)
            .stream()
            .map(FiltroSql::toString)
            .forEach(log::info);
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

    @Test
    public void testePreencherConfigQuerySqlComFiltroSql() {
        String sql1 = "SELECT * FROM TMP_ENTRADA_PAGTO WHERE ${PBACCTG} = ${string} AND ${PBACCT} IN (${string[]}) ORDER BY PBID ASC";
        String sql2 = "SELECT * FROM TMP_ENTRADA_PAGTO WHERE ${PBACCTG} = ${string} AND ${PBACCT}";
        String sql3 = "SELECT * FROM TMP_ENTRADA_PAGTO WHERE ${PBACCTG} = ${string} AND ${PBACCT} IN (${string[]}) AND ${PBACCT} = ${string}";
        val filtroSql1 = new FiltroSql("PBACCTG", "string");
        filtroSql1.setValor("1");
        val filtroSql2 = new FiltroSql("PBACCT", "string[]");
        filtroSql2.setValor("215487621-BRM");
        val listaFiltrosSql = List.of(filtroSql1, filtroSql2);

        String resultado = FiltroSql.montarSql(sql1,listaFiltrosSql);
        log.info(resultado);

        resultado = FiltroSql.montarSql(sql2,listaFiltrosSql);
        log.info(resultado);

        resultado = FiltroSql.montarSql(sql3,listaFiltrosSql);
        log.info(resultado);
    }

}
