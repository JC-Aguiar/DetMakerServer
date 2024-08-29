package br.com.ppw.dma.config;

import br.com.ppw.dma.domain.storage.StorageService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@EnableAspectJAutoProxy
@Configuration
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
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {};
//            storageService.deleteAll();
//            storageService.init();
//        };
    }

}
