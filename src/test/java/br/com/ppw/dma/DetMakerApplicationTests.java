package br.com.ppw.dma;

import br.com.ppw.dma.master.MasterOracleDAO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class DetMakerApplicationTests {

    @Autowired MasterOracleDAO oracleDao;

    @Test
    public void testeColetarCamposEValoresDeQueryDinamicaNativa() {
        oracleDao.teste();
    }

}
