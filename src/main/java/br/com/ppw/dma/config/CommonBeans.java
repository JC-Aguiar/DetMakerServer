package br.com.ppw.dma.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class CommonBeans {

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

//    /**
//     * Operações automáticas a serem executadas após inicialização do Spring
//     * @param storageService {@link StorageService} gerenciado pelo Spring
//     * @return {@link CommandLineRunner} gerenciado pelo Spring
//     */
//    @Bean
//    CommandLineRunner init(StorageService storageService, JobRepository jobDao) {
////        log.info("Iniciando rotina de sanitização dos Jobs.");
//        return (args) -> { };
////            storageService.deleteAll();
////            storageService.init();
//    }

    @Bean
    public ThreadPoolTaskExecutor setTaskExecutorDosExecucoesDePipelines() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);  // Mínimo número de threads na pool (principais)
        executor.setMaxPoolSize(Integer.MAX_VALUE);  // Máximo número de threads na pool
        executor.setAllowCoreThreadTimeOut(false);
        executor.setQueueCapacity(0);  // Capacidade da fila de tarefas por thread
        executor.setKeepAliveSeconds(60);  // Mantenha threads ociosos por 60 segundos
        executor.setThreadNamePrefix("Task-Ambiente-"); // Nome da thread
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // Política de rejeição. Aqui, se o pool estiver cheio a tarefa roda na thread do chamador
        executor.setDaemon(true); // Se as threads devem ser interrompidas se o App fechar
        executor.setWaitForTasksToCompleteOnShutdown(true); // Aguarda tarefas no shutdown
        executor.setAwaitTerminationSeconds(30); // Segundos de espera ap´so receber shutdown
        executor.initialize(); // Iniciazar
        return executor;
    }

}
