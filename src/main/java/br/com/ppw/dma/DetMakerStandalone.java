package br.com.ppw.dma;

import br.com.ppw.dma.config.StorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Slf4j
@Component
@SpringBootApplication
@EnableWebMvc
@EnableCaching
@ComponentScan
@EnableConfigurationProperties(StorageProperties.class)
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
public class DetMakerStandalone {

	public DetMakerStandalone() {
	}

	public static void main(String[] args) {
		SpringApplication.run(DetMakerStandalone.class, args);
	}

}
