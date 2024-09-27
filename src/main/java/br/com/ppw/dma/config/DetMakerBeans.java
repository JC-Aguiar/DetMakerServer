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
        log.info("Iniciando rotina de sanitização dos Jobs.");
        return (args) -> {
//            var jobs = jobDao.findAll();
//            jobs.forEach(job -> {
//                job.setMascaraEntrada(
//                    FormatString.dividirValores(job.getMascaraEntrada())
//                        .stream()
//                        .map(FormatString::extrairMascara)
//                        .collect(Collectors.joining(", "))
//                );
//                job.setMascaraLog(
//                    FormatString.dividirValores(job.getMascaraLog())
//                        .stream()
//                        .map(FormatString::extrairMascara)
//                        .collect(Collectors.joining(", "))
//                );
//                job.setMascaraSaida(
//                    FormatString.dividirValores(job.getMascaraSaida())
//                        .stream()
//                        .map(FormatString::extrairMascara)
//                        .collect(Collectors.joining(", "))
//                );
//            });
//            jobDao.saveAll(jobs);
        };
//            storageService.deleteAll();
//            storageService.init();
    }

//    @Bean(name = "dynamicThreadPool")
//    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(1);  // Minimum number of threads in the pool
//        executor.setMaxPoolSize(12);  // Maximum number of threads in the pool
////        executor.setQueueCapacity(20);  // Queue capacity for pending tasks
//        executor.setKeepAliveSeconds(60);  // Keep idle threads alive for 60 seconds
//        executor.setThreadNamePrefix("QueueExecutor-");  // Thread name prefix
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // Handle rejection
//        executor.setDaemon(true);
//        executor.initialize();
//        return executor;
//    }

}
