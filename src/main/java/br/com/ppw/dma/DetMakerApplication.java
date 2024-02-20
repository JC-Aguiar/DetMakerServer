package br.com.ppw.dma;

import br.com.ppw.dma.exception.DiretorioSemPermissaoException;
import br.com.ppw.dma.system.StorageProperties;
import br.com.ppw.dma.system.StorageService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
@SpringBootApplication
@EnableWebMvc
@EnableCaching
@EnableJpaRepositories
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@EnableConfigurationProperties(StorageProperties.class)
public class DetMakerApplication { //extends SpringBootServletInitializer {

	@Getter
	private static String appVersion;

	@GetMapping("/")
	public String home() {
		return "Bem vindo ao DET-MAKER-SERVER";
	}


	public static void main(String[] args) throws DiretorioSemPermissaoException, IOException {
		log.info("Obtendo metadados da aplicação DET-MAKER.");
		setAppVersion();

//		log.info("Preparando servidor do DET-MAKER");
//		log.info("Validando diretórios e arquivos obrigatórios...");
//		Arquivos.validarCriarDiretorio(DIR_RECURSOS);

		log.info("Iniciando servidor do DET-MAKER");
		SpringApplication.run(DetMakerApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

	private static void setAppVersion() throws IOException {
		if(appVersion != null) return;

		//if(checkIdeaEnvironment()) {
		appVersion = DetMakerApplication.class.getPackage().getImplementationVersion();
		if(appVersion == null) appVersion = "vDEV.Unknown";

		log.info("Versão da aplicação: {}", appVersion);
	}

	private static boolean checkIdeaEnvironment() {
		val ideas = List.of("netbeans", "eclipse", "intellij");
		val classPath = System.getProperty("java.class.path");
		log.debug("ClassPath:");

		return Arrays.stream(classPath.split(";"))
			.peek(cp -> log.debug(" - {}", cp))
			.anyMatch(cp -> ideas.stream()
				.anyMatch(idea -> idea.equalsIgnoreCase(cp)));
	}

	private static String getVersionFromApplicationContext() throws IOException {
		val resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(
			new PathMatchingResourcePatternResolver());
		val resources = resourcePatternResolver.getResources("classpath*:META-INF/MANIFEST.MF");

		for(Resource resource : resources) {
			val properties = new Properties();
			properties.load(resource.getInputStream());
			val version = properties.getProperty("Implementation-Version");
			if(StringUtils.hasText(version)) {
				return version;
			}
		}
		return null;
	}

}
