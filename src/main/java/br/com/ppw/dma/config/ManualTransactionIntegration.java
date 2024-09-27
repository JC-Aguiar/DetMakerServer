package br.com.ppw.dma.config;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ThreadPoolExecutor;

import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

@Component
@Transactional(propagation = NOT_SUPPORTED) // Iremos tratar a transação manualmente
public class ManualTransactionIntegration {

    private PlatformTransactionManager transactionManager;
    private EntityManager entityManager;
    private TransactionTemplate transactionTemplate;
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Autowired
    public ManualTransactionIntegration(
        PlatformTransactionManager transactionManager,
        EntityManager entityManager) {

        this.transactionManager = transactionManager;
        this.entityManager = entityManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public ThreadPoolTaskExecutor executar(long ambienteId) {
        if(threadPoolTaskExecutor != null) return threadPoolTaskExecutor;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);  // Mínimo número de threads na pool
        executor.setMaxPoolSize(1);  // Máximo número de threads na pool
//        executor.setQueueCapacity(20);  // Capacidade da fila de tarefas por thread
        executor.setKeepAliveSeconds(60);  // Mantenha tópicos ociosos por 60 segundos
        executor.setThreadNamePrefix("Queue-Ambiente-" + ambienteId); // Nome da thread
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // ?
        executor.setDaemon(true); // Se as threads devem ser interrompidas se o App fechar
        executor.initialize(); // Iniciazar
        return executor;
    }

}
