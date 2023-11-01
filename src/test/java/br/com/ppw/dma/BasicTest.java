package br.com.ppw.dma;

import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.util.ComandoSql;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.util.FormatString.dividirValores;

@Slf4j
public class BasicTest {

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
