package br.com.ppw.dma;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static br.com.ppw.dma.util.FormatString.dividirValores;

//@SpringBootTest
@Slf4j
class DetMakerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void dividirStringEmLista() {
		val tabelas = "TB_TEMP_CARGA_4P, TB_CONTROLE_SEQ, TB_ARQENTRADA";
		val listaTabelas = dividirValores(tabelas);
		listaTabelas.forEach(log::info);
	}

}
