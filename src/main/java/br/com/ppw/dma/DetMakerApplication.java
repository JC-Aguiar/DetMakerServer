package br.com.ppw.dma;

import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.exception.DiretorioSemPermissaoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Clock;
import java.time.Duration;

@Slf4j
@SpringBootApplication
@EnableWebMvc
@EnableCaching
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
public class DetMakerApplication {

	public static final String DIR_RECURSOS = "temp/";
	public static final Clock RELOGIO = Clock.tick(Clock.systemDefaultZone(), Duration.ofMillis(1));

	public static void main(String[] args) throws DiretorioSemPermissaoException {
		log.info("Preparando servidor do DET-MAKER");
		log.info("Validando diretórios e arquivos obrigatórios...");
		Arquivos.validarCriarDiretorio(DIR_RECURSOS);

		log.info("Iniciando servidor do DET-MAKER");
		SpringApplication.run(DetMakerApplication.class, args);
	}

}