package br.com.ppw.dma;

import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.job.ComandoSql;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class DetMakerApplicationTests {

	@Autowired
	EvidenciaService evidenciaService;

	@Test
	void testandoExtracaoBanco() {
		val campos = List.of( "DMACCT"); //"DMACCTG",
		val tabela = "DELQMST";

		val comandosSql = List.of(new ComandoSql(null, tabela, null));
		evidenciaService.extractTable(comandosSql);
	}

}
