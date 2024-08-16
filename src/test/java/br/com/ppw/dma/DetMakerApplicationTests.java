package br.com.ppw.dma;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.master.MasterOracleDAO;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@SpringBootTest
@Slf4j
class DetMakerApplicationTests {

    MasterOracleDAO oracleDao;

    @Autowired ResourceLoader resourceLoader;


    @Test
    public void testeObterRecursoInterno() throws IOException {
        val arquivoNome = "template/template.html";
        log.info("Procurando pelo recurso '{}'.", arquivoNome);

        val recurso = resourceLoader.getResource("classpath:" + arquivoNome);
        val conteudo = new StringBuilder();

        try (val in = new InputStreamReader(recurso.getInputStream(), StandardCharsets.UTF_8);
             val reader = new BufferedReader(in)) {
            //-------------------------------------------------------------------------------
            String line;
            while((line = reader.readLine()) != null) {
                conteudo.append(line);
            }
        }
        log.info(conteudo.toString());
    }



}
