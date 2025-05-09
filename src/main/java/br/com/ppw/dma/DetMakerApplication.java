package br.com.ppw.dma;

import br.com.ppw.dma.config.StorageProperties;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
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
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Slf4j
@Component
@SpringBootApplication
@EnableWebMvc
@EnableCaching
@ComponentScan
@EnableJpaRepositories
@EnableConfigurationProperties(StorageProperties.class)
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
public class DetMakerApplication extends SpringBootServletInitializer {

	public DetMakerApplication() {
	}

	public static void main(String[] args) {
		SpringApplication.run(DetMakerApplication.class, args);
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
