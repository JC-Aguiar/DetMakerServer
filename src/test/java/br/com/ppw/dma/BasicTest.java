package br.com.ppw.dma;

import br.com.ppw.dma.config.DatabaseConfig;
import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.pipeline.PipelineRelatorioDTO;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.user.UserInfoDTO;
import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.HtmlDet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.DetMakerApplication.RELOGIO;
import static br.com.ppw.dma.util.FormatString.dividirValores;

@Slf4j
public class BasicTest {

    @Test
    public void testeGerandoHtmlDetUnificado() throws IOException, URISyntaxException {
        val dataHoraHoje = OffsetDateTime.now(RELOGIO);
        val evidenciasDTO = List.of(
            EvidenciaInfoDTO.builder()
                .job("cy3_rem_notif.ksh")
                .jobDescricao("Processa REMESSA NOTIFICACOES REGULATORIAS PARA SMARTBILL")
                .data(dataHoraHoje.minusSeconds(32548))
                .sucesso(true)
                .ordem(0)
                .argumentos("20230822")
                .queries(List.of("SELECT * FROM SEQUENCIA s WHERE TPARQ='B023'"))
                .tabelasPreJob(List.of("[#1] TPARQ=B023, SEQ=169"))
                .tabelasPosJob(List.of("[#1] TPARQ=B023, SEQ=170"))
                .logs(List.of("""
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                    Integer sit amet justo sit amet lacus faucibus viverra. 
                    
                    Nunc nisi neque, volutpat vitae vulputate et, interdum et enim. 
                    Morbi ac quam tempus, consequat dolor et, tincidunt felis. Pellentesque pulvinar suscipit mauris sed dapibus. Nulla convallis leo eu eleifend euismod. Aliquam in ultricies libero. Nam tincidunt felis augue, eu mattis erat commodo at.
                    Curabitur eleifend iaculis metus, vitae varius nisl dignissim accumsan. 
                    
                    In ut nisi leo. Sed facilisis vitae libero in ultrices. Integer elit ante, efficitur sit amet aliquet eget, dapibus nec mauris. Donec luctus ut tellus vitae mollis. Cras tempus libero eu facilisis scelerisque. Nam in viverra tellus. Nulla auctor justo quis dolor rutrum, a faucibus enim fermentum. Nullam posuere leo tortor, at laoreet purus tristique ut. Proin at quam elit. Aliquam sed est suscipit, dapibus nibh eget, dapibus nunc. Praesent pellentesque libero eget lobortis euismod. Duis commodo eu ante et dapibus. Praesent mollis consectetur diam sed volutpat. Nunc non vehicula arcu.
                    
                    Vivamus at tempus elit. Suspendisse vitae sollicitudin magna. 
                    Sed varius diam sit amet porttitor pulvinar. Donec felis ipsum, finibus ac vulputate vel, fermentum vitae diam. Duis aliquam pharetra orci et sagittis. Proin tincidunt ex a laoreet dictum. Nulla non ante bibendum, molestie ante in, hendrerit arcu. 
                    
                    Praesent pellentesque, lacus sed consectetur convallis, mi est facilisis eros, id eleifend leo ipsum nec velit. Duis finibus erat at velit porta sollicitudin.
                    """))
                .logsNome(List.of("cyb_encrypt_20230621_011223.log"))
                .build()
        );
        val relatorioDto = RelatorioHistoricoDTO.builder()
            .nomeProjeto("Teste Unitário DET-MAKER")
            .nomeAtividade("testeGerandoHtmlDetUnificado")
            .configuracao("Nenhuma")
            .dataInicio(dataHoraHoje.minusMinutes(3))
            .dataFim(dataHoraHoje)
            .evidencias(evidenciasDTO)
            .sucesso(false)
            .build();

        val pipelineRelatorio = new PipelineRelatorioDTO(
            "Teste 01",
            "Tentando criar documento DET com HTML, CSS e JS unificados",
            relatorioDto);

        val dbConfig = new DatabaseConfig();
        dbConfig.setDbAmbiente("DEV");
        dbConfig.setDbSistema("Vivo Cyber 3");

        val usersInfo = List.of(UserInfoDTO.builder()
            .nome("João Aguiar")
            .papel("DEV")
            .empresa("Peopleware")
            .email("joao.aguiar@ppware.com.br")
            .telefone("(13) 988465656")
            .build());

        HtmlDet.gerarNovoDet(pipelineRelatorio, dbConfig, usersInfo);
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
        val logEvidencia = service.getLogMaisRecente(logsAntes, logsDepois);
        if(logEvidencia.isEmpty()) {
            //throw new RuntimeException("Nenhum arquivo de log disponível para esse job.");
            log.warn("Nenhum arquivo de log disponível");
        }
        else {
            log.info("Logs coletados como Evidência: ");
            logEvidencia.forEach(service::printArquivo);
        }
    }


}
