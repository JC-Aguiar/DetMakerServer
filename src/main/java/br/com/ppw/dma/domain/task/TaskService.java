package br.com.ppw.dma.domain.task;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.evidencia.EvidenciaService;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.task.result.PipelineResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static br.com.ppw.dma.domain.task.TaskStatus.*;
import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static java.util.stream.Collectors.toSet;

//TODO: javadoc todos os métodos da classe
@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskService {

    @PersistenceContext EntityManager entityManager;
    ObjectMapper objectMapper;
    JobService jobService;
    EvidenciaService evidenciaService;
    AmbienteService ambienteService;
    PlatformTransactionManager transactionManager;
    ThreadPoolTaskExecutor executor;
    ConcurrentLinkedDeque<RemoteTask> allTasks = new ConcurrentLinkedDeque<>();
//    Map<Long, UUID> executorsPerAmbienteId = new ConcurrentHashMap<>();

//    static final int SAME_PIPELINE_TOLERANCE_SECONDS = 30;
//    static final int TASK_QUEUE_LIMIT = 3;


    @Autowired
    public TaskService(
        EntityManager entityManager,
        ObjectMapper objectMapper,
        JobService jobService,
        EvidenciaService evidenciaService,
        AmbienteService ambienteService,
        PlatformTransactionManager transactionManager)
//        @Qualifier("multiAmbienteTaskExecutor") ThreadPoolTaskExecutor executor)
    {
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.jobService = jobService;
        this.evidenciaService = evidenciaService;
        this.ambienteService = ambienteService;
        this.transactionManager = transactionManager;
        this.executor = setTaskExecutorDosExecucoesDePipelines();

        this.entityManager.setFlushMode(FlushModeType.COMMIT);
        //TODO: processar próximos registros pendentes?
    }

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

    @Transactional(noRollbackFor = Throwable.class)
    public TaskPushResponseDTO pushTaskToQueue(
        @NonNull Ambiente ambiente,
        @NonNull String usuario,
        @NonNull TaskPayload payload)
    throws JsonProcessingException {
        var ambienteId = ambiente.getId();
        var totalTasksForAmbiente = countTasksByAmbiente(ambienteId);
        var isNewTaskNextToRun = totalTasksForAmbiente < 1;
        var newTaskStatus = (isNewTaskNextToRun ? EXECUTANDO : AGUARDANDO);

        //TODO: validar se a nova Task já consta em memória nos último 30 segundos (mesma Pipeline e mesmo Usuário)

        var task = RemoteTask.builder()
            .ambienteId(ambienteId)
            .pipelineNome(payload.getPipelineNome())
            .usuario(usuario)
            .payload(objectMapper.writeValueAsString(payload))
            .dataSolicitacao(OffsetDateTime.now(RELOGIO))
            .status(newTaskStatus)
            .build();
        addTask(task);

        if(isNewTaskNextToRun) {
            log.info("Gerando nova thread para executar as Tasks do Ambiente ID {}.", ambienteId);
            executor.execute(() -> startTask(task));
        }
        return TaskPushResponseDTO.builder()
            .ambienteId(ambienteId)
            .queueSize(totalTasksForAmbiente)
            .ticket(task.getTicket())
            .status(task.getStatus())
            .build();
    }

    public void addTask(@Valid RemoteTask task) {
        log.info("Adicionando Task '{}' para Ambiente ID {}.", task.getTicket(), task.getAmbienteId());
        allTasks.add(task);
    }

    public Optional<RemoteTask> removeTask(RemoteTask task) {
        if(task == null) return Optional.empty();

        log.info("Removendo Task '{}' do Ambiente ID {}.", task.getTicket(), task.getAmbienteId());
        allTasks.remove(task);
        synchronized(allTasks) {
            return allTasks.stream()
                .filter(t -> Objects.equals(t.getAmbienteId(), task.getAmbienteId()))
                .min(Comparator.comparing(RemoteTask::getDataSolicitacao));
//            oldestTask.ifPresent(allTasks::remove);
//            return oldestTask;
        }
    }

    public long countTasksByAmbiente(Long ambienteId) {
        synchronized(allTasks) {
            return allTasks.stream()
                .filter(t -> Objects.equals(t.getAmbienteId(), ambienteId))
                .count();
        }
    }

    @Profile("test")
    public void waitForAllTasksCompletion() {
        while(!allTasks.isEmpty()) { }
        executor.submitCompletable(() -> log.info("Todas as Tasks finalizaram.")).join();
    }

    public List<RemoteTask> getTasksByAmbiente(Long ambienteId) {
        synchronized(allTasks) {
            return allTasks.stream()
                .filter(t -> Objects.equals(t.getAmbienteId(), ambienteId))
                .collect(Collectors.toList());
        }
    }


    public Page<RemoteTask> findAllByExample(RemoteTask exemplo, @NonNull Pageable pageConfig) {
        if(exemplo == null) {
            return new PageImpl<>(List.of(), pageConfig, 0);
        }
        synchronized(allTasks) {
            var result = allTasks.stream()
                .filter(task -> Optional.ofNullable(exemplo.getTicket())
                    .map(ticket -> ticket.equalsIgnoreCase(task.getTicket()))
                    .orElse(true))
                .filter(task -> Optional.ofNullable(exemplo.getAmbienteId())
                    .map(ambiente -> ambiente.equals(task.getAmbienteId()))
                    .orElse(true))
                .filter(task -> Optional.ofNullable(exemplo.getPipelineNome())
                    .map(pipeline -> pipeline.equalsIgnoreCase(task.getPipelineNome()))
                    .orElse(true))
                .filter(task -> Optional.ofNullable(exemplo.getUsuario())
                    .map(user -> user.equalsIgnoreCase(task.getUsuario()))
                    .orElse(true))
                .filter(task -> Optional.ofNullable(exemplo.getDataSolicitacao())
                    .map(OffsetDateTime::toLocalDate)
                    .map(data -> data.isEqual(task.getDataSolicitacao().toLocalDate()))
                    .orElse(true))
                .toList();
            return new PageImpl<>(result, pageConfig, result.size());
        }
    }

    private void startTask(@NonNull RemoteTask originalTask) {
        val taskRef = new AtomicReference<>(originalTask);

        while(taskRef.get() != null) {
            val task = taskRef.get();
            var ticket = task.getTicket();
            var ambienteId = task.getAmbienteId();

            log.info("TASK '{}' - AMBIENTE ID {} --- START", ticket, ambienteId);
            task.setStatus(EXECUTANDO);
            task.setDataExecucao(OffsetDateTime.now(RELOGIO));
            log.info(task.toString());

            var transaction = new TransactionTemplate(transactionManager);
            try {
                var payload = objectMapper.readValue(task.getPayload(), TaskPayload.class);
                transaction.execute(status -> {
                    executeTaskPipeline(task, payload);
                    return null;
                });
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            log.info("Deletando Task '{}' após execução.", task.getTicket());
            task.setStatus(FINALIZANDO);
            taskRef.set(removeTask(task).orElse(null)); //TODO: REVISAR!!!!!!!!!!!!!!!
            log.info("TASK '{}' - AMBIENTE ID {} --- END", ticket, ambienteId);
        }
    }

    @Transactional
    private PipelineResult executeTaskPipeline(@NonNull RemoteTask task, @NonNull TaskPayload payload) {
        var ticket = task.getTicket();
        var usuario = task.getUsuario();
        var ambienteId = task.getAmbienteId();

        var ambiente  = ambienteService.findById(ambienteId);
        var cliente = ambiente.getCliente();
        var banco = ambiente.acessoBanco();
        var sftp = ambiente.acessoFtp();
        var pipelineResult = PipelineResult.builder()
            .ambiente(ambiente)
            .pipelineNome(payload.getPipelineNome())
            .pipelineDescricao(payload.getPipelineDescricao())
            .ticket(ticket)
            .usuario(usuario)
            .clienteNome(cliente.getNome())
            .build();

        try {
            if(!payload.getQueriesPrePipeline().isEmpty()) {
                var queriesAntes = payload.getQueriesPrePipeline()
                    .stream()
                    .map(TaskPayloadQuery::getQuery)
                    .collect(toSet());
                ambienteService.runQuery(queriesAntes, banco);
            }
            var jobsResult = jobService.executar(banco, sftp, payload.getJobs());
            pipelineResult.addJobResult(jobsResult);

            if(!payload.getQueriesPosPipeline().isEmpty()) {
                var queriesDepois = payload.getQueriesPosPipeline()
                    .stream()
                    .map(TaskPayloadQuery::getQuery)
                    .collect(toSet());
                ambienteService.runQuery(queriesDepois, ambiente.acessoBanco());
            }
            //Return the same instance
            pipelineResult = evidenciaService.gerarEvidencia(pipelineResult);
        }
        catch(Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            pipelineResult.setErro(true);
            pipelineResult.setMensagemErro(e.getMessage());
        }
        return pipelineResult;
    }
}
