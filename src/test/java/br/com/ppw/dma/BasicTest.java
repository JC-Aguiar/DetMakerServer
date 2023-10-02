package br.com.ppw.dma;

import br.com.ppw.dma.job.ComandoSql;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;

import static br.com.ppw.dma.util.FormatString.dividirValores;

@Slf4j
public class BasicTest {

    @Test
    public void testeDeColetaGenericaAoBanco() {
        val comandoSql = new ComandoSql();
        comandoSql.setQuery("SELECT * FROM EVENTOS_WEB ew WHERE EVTYPE='EV_BOLETO_CYBER_HUBPGTO' ORDER BY EVID");
        comandoSql.setTabela("EVENTOS_WEB");
    }

    @Test
    public void dividirStringEmLista() {
        val tabelas = "TB_TEMP_CARGA_4P, TB_CONTROLE_SEQ, TB_ARQENTRADA";
        val listaTabelas = dividirValores(tabelas);
        listaTabelas.forEach(log::info);
    }

}
