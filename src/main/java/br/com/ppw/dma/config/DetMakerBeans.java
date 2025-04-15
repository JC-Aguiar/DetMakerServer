package br.com.ppw.dma.config;

import br.com.ppw.dma.domain.job.JobRepository;
import br.com.ppw.dma.domain.storage.StorageService;
import br.com.ppw.dma.util.FormatString;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
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

    /**
     * Gera um bean de {@link ModelMapper} feito para conversões restritivas.
     * Ideal para gerar uma nova classe do zero com base numa (outra) classe já preenchida.
     * @return {@link ModelMapper}.
     */
    @Bean("Basic")
    public ModelMapper modelMapper() {
        var mapper = new ModelMapper();
        mapper.getConfiguration().setSkipNullEnabled(true);
        return mapper;
    }

    /**
     * Gera um bean de {@link ModelMapper} feito para conversões flexíveis.
     * Ideal para atualizar uma classe já existente com base numa (outra) classe também
     * já preenchida.
     * @return {@link ModelMapper}.
     */
    @Bean("Flex")
    public ModelMapper flexModelMapper() {
        var mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        mapper.getConfiguration().setSkipNullEnabled(true);
        return mapper;
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
