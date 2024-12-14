package br.com.ppw.dma.config;

import br.com.ppw.dma.domain.job.JobRepository;
import br.com.ppw.dma.domain.storage.StorageService;
import br.com.ppw.dma.util.FormatString;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
public class DetMakerBeans {

//    @Bean(name = "multipartResolver")
//    public CommonsMultipartResolver multipartResolver() {
//        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
//        multipartResolver.setMaxUploadSize(100000);
//        return multipartResolver;
//    }

    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Operações automáticas a serem executadas após inicialização do Spring
     * @param storageService {@link StorageService} gerenciado pelo Spring
     * @return {@link CommandLineRunner} gerenciado pelo Spring
     */
    @Bean
    CommandLineRunner init(StorageService storageService, JobRepository jobDao) {
//        log.info("Iniciando rotina de sanitização dos Jobs.");
        return (args) -> { };
//            storageService.deleteAll();
//            storageService.init();
    }

}
