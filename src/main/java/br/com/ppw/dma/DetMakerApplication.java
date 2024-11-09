package br.com.ppw.dma;

import br.com.ppw.dma.config.StorageProperties;
import com.github.lalyos.jfiglet.FigletFont;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

@Slf4j
@SpringBootApplication
@EnableWebMvc
@EnableCaching
@EnableJpaRepositories
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@EnableConfigurationProperties(StorageProperties.class)
@ComponentScan
@RestController("/")
public class DetMakerApplication extends SpringBootServletInitializer {

	@Getter
	private static String appVersion;


	@GetMapping("home")
	public String hello() {
		return "DET-MAKER SERVER IS ONLINE.";
	}

	public static void main(String[] args) {
		SpringApplication.run(DetMakerApplication.class, args);
		setAppVersion();
		try {
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

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DetMakerApplication.class);
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
	}

}
