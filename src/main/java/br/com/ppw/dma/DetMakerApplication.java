package br.com.ppw.dma;

import br.com.ppw.dma.config.CustomPrintStream;
import br.com.ppw.dma.exception.DiretorioSemPermissaoException;
import br.com.ppw.dma.system.StorageProperties;
import br.com.ppw.dma.system.StorageService;
import com.github.lalyos.jfiglet.FigletFont;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

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


	public static void main(String[] args) {
		CustomPrintStream customPrintStream = new CustomPrintStream(System.out);
		System.setOut(customPrintStream);

		SpringApplication.run(DetMakerApplication.class, args);
		setAppVersion();
		try {
			//String fonte = FigletFont.convertOneLine("standard.flf");
			String[] banner = FigletFont.convertOneLine("DET-MAKER").split("\n");
			System.out.println();
			Arrays.stream(banner)
				.filter(linha -> !linha.trim().isEmpty())
				.forEach(System.out::println);
		}
		catch(IOException e) {
			System.err.println(e.getMessage());
		}
		System.out.println(":: Det-Maker ::                 (" +appVersion+ ")");
	}

	private static void setAppVersion() {
		if(appVersion != null) return;
		appVersion = DetMakerApplication.class.getPackage().getImplementationVersion();
		if(appVersion == null)
			appVersion = "v" + LocalDateTime.now().format(BASIC_ISO_DATE) + "-DEV";
	}


}
