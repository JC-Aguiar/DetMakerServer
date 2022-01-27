package br.com.jcaguiar.cinephiles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CinephilesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinephilesApplication.class, args);
	}

}
