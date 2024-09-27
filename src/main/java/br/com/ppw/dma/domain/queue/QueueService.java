package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.cliente.ClienteService;
import br.com.ppw.dma.domain.evidencia.EvidenciaService;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.queue.result.PipelineResult;
import br.com.ppw.dma.domain.relatorio.RelatorioService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Slf4j
@Service
@EnableAsync
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QueueService extends MasterService<Long, TaskQueue, QueueService> {

    ApplicationEventPublisher publisher;
    ObjectMapper objectMapper;
    QueueRepository queueDao;
    JobService jobService;
    EvidenciaService evidenciaService;
    RelatorioService relatorioService;
    AmbienteService ambienteService;
    ClienteService clienteService;
    EntityManager entityManager;
    PlatformTransactionManager transactionManager;
//    Queue<QueuePushResponseDTO> fila = new ConcurrentLinkedQueue<>();
//    Map<Long, Queue<QueuePushResponseDTO>> queueMap = new ConcurrentHashMap<>();
//    ExecutorService motor = Executors.newSingleThreadExecutor();
    Map<Long, ThreadPoolTaskExecutor> mapaFilas = new ConcurrentHashMap<>();


//    private record ExecutorQueue(ExecutorService executor, Queue<QueuePushResponseDTO> fila) {
//        private static ExecutorQueue start(QueuePushResponseDTO itemFila) {
//            var novoExec = new ExecutorQueue(
//                Executors.newSingleThreadExecutor(),
//                new ConcurrentLinkedQueue<>());
//            novoExec.fila.offer(itemFila);
//            return novoExec;
//        }
//    }

    @Autowired
    public QueueService(
        ApplicationEventPublisher publisher,
        ObjectMapper objectMapper,
        QueueRepository queueDao,
        JobService jobService,
        EvidenciaService evidenciaService,
        RelatorioService relatorioService,
        AmbienteService ambienteService,
        ClienteService clienteService,
        EntityManager entityManager,
        PlatformTransactionManager transactionManager) {

        super(queueDao);
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.queueDao = queueDao;
        this.jobService = jobService;
        this.evidenciaService = evidenciaService;
        this.relatorioService = relatorioService;
        this.ambienteService = ambienteService;
        this.clienteService = clienteService;
        this.entityManager = entityManager;
        this.transactionManager = transactionManager;
//        this.motor.execute(this::gerenciarExecucoes);
//        this.filaProcessador.execute(this::tratamentoDaFila);
    }


    public Long countByStatusInAmbiente(
        @NonNull Ambiente ambiente,
        @NonNull QueueStatus status) {

        return queueDao.countByStatusInAmbiente(ambiente, status.name());
    }

    public Long countInAmbiente(@NonNull Ambiente ambiente) {
        return queueDao.countInAmbiente(ambiente.getId());
    }


    @Transactional(noRollbackFor = Throwable.class)
    public QueuePushResponseDTO pushQueue(
        @NonNull Ambiente ambiente,
        @NonNull String usuario,
        @NonNull QueuePayload payload)
    throws JsonProcessingException, DuplicatedRecordException {
        var ambienteId = ambiente.getId();
        var executorService = Optional.ofNullable(mapaFilas.get(ambienteId))
            .map(task -> {
                log.info("Executor já disponível no mapa de threads para Ambiente ID {}.", ambienteId);
                return task;
            })
            .orElseGet(() -> {
                log.info("Criando novo executor no mapa de threads para Ambiente ID {}.", ambienteId);
                var novoExec = novoTaskExecutor(ambienteId);
                mapaFilas.put(ambienteId, novoExec);
                return novoExec;
            });
        var tamanhoFila = executorService.getQueueSize();
        var resposta = new QueuePushResponseDTO(ambienteId, tamanhoFila);
        var json = objectMapper.writeValueAsString(payload);
        var task = TaskQueue.builder()
            .ticket(resposta.getTicket())
            .ambiente(ambiente)
            .pipeline(payload.getPipelineNome())
            .usuario(usuario)
            .payload(json)
            .dataSolicitacao(OffsetDateTime.now(RELOGIO))
            .status(QueueStatus.AGUARDANDO)
            .build();
        save(task);
        executorService.execute(() -> tratamentoDaFila(resposta));
        return resposta;
    }


    public TaskQueue saveAndFlush(@NonNull TaskQueue entity) throws DuplicatedRecordException {
        return queueDao.saveAndFlush(entity);
    }

    public void deleteAndFlush(@NonNull TaskQueue entity) {
        delete(entity);
        queueDao.flush();
    }

    private void tratamentoDaFila(@NonNull QueuePushResponseDTO queueDto) {
        //TODO: TRABALHAR COM SESSÃO MANUAL AQUI! REESCREVER TODO O CÓDIGO DA GERAÇÃO DE EVIDÊNCIAS...

        var ticket = queueDto.getTicket();
        log.info("Iniciando tarefa do ticket: '{}'.", ticket);
        var transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute((status) -> {
            TaskQueue taskQueue = null;
            Ambiente ambiente = null;
            Cliente cliente = null;
//            var session = entityManager.unwrap(Session.class);
//            var transaction = session.beginTransaction();
//            transaction.begin();;
            try {
            var sql =   "SELECT q, a, c " +
                        "FROM PPW_QUEUE q " +
                        "JOIN q.ambiente a " +
                        "JOIN a.cliente c " +
                        "WHERE q.ticket = :ticket";
            log.info("SQL: {}", sql);

            var dados = (Object[]) entityManager.createQuery(sql)
                .setParameter("ticket", ticket)
                .getResultList()
                .stream()
                .findFirst()
                .orElseThrow(); //TODO: mudar para exception própria
            taskQueue = (TaskQueue) dados[0];
            ambiente = (Ambiente) dados[1];
            cliente = (Cliente) dados[2];
            log.info(taskQueue.toString());
            log.info(ambiente.toString());
            log.info(cliente.toString());
//            var projection = queueDao.findByTicket(ticket).orElseGet(() -> null);

////        if(taskQueue == null) {
//            if(projection == null) {
//                log.warn("Nenhum registro encontrado no banco para ticket: '{}'.", ticket);
//                return;
//            }

                log.info("Desserializando JSON para {}.", QueuePayload.class.getSimpleName());
                var payload = objectMapper.readValue(taskQueue.getPayload(), QueuePayload.class);
                log.info(payload.toString());

                taskQueue.setPayloadObj(payload);
                taskQueue.setStatus(QueueStatus.EXECUTANDO);
                taskQueue.setDataExecucao(OffsetDateTime.now(RELOGIO));
                saveAndFlush(taskQueue); //TODO: fluxsh ainda necessário?

                var pipelineResult = runQueue(taskQueue, ambiente, cliente);
                val evidenciasResult = evidenciaService.gerarEvidencia(pipelineResult);
                pipelineResult.addEvidenciaResult(evidenciasResult);
                relatorioService.buildAndPersist(taskQueue.getAmbiente(), pipelineResult);
            }
            catch(NoSuchElementException e) {
                log.warn("Nenhum registro encontrado no banco para ticket: '{}'.", ticket);
            }
            catch(Exception e) {
                e.printStackTrace();
                log.error("Erro inesperado nas filas de Task: {}", e.getMessage());
            }
            finally {
                entityManager.remove(taskQueue);
                entityManager.flush();
//            deleteAndFlush(taskQueue); //TODO: flush ainda necessário?
//                transaction.commit();
//                session.close();
            }
            return null;
        });
    }

    @Transactional
    private PipelineResult runQueue(
        @NonNull TaskQueue itemFila,
        @NonNull Ambiente ambiente,
        @NonNull Cliente cliente) {

        var ticket = itemFila.getTicket();
        var payload = itemFila.getPayloadObj();
        var usuario = itemFila.getUsuario();
        //Por problemas de sessão, os objetos abaixo precisaram ser coletados/montados manualmente
//        var ambiente = ambienteService.findById(
//            itemFila.getAmbiente().getId());
//        var cliente  = clienteService.findByAmbienteId(
//            itemFila.getAmbiente().getId());
        var banco = new AmbienteAcessoDTO(
            ambiente.getConexaoBanco(),
            ambiente.getUsuarioBanco(),
            ambiente.getSenhaBanco());
        var sftp = new AmbienteAcessoDTO(
            ambiente.getConexaoSftp(),
            ambiente.getUsuarioSftp(),
            ambiente.getSenhaSftp());

        var pipelineResult = PipelineResult.builder()
            .pipelineNome(payload.getPipelineNome())
            .pipelineDescricao(payload.getPipelineDescricao())
            .ticket(ticket)
            .usuario(usuario)
            .clienteNome(cliente.getNome())
            .build();

        try {
            var queriesAntes = payload.getQueriesPrePipeline()
                .stream()
                .map(QueuePayloadQuery::getQuery)
                .collect(Collectors.toSet());
            ambienteService.runQuery(queriesAntes, banco);

            var jobsResult = jobService.executar(banco, sftp, payload.getJobs());
            pipelineResult.addJobResult(jobsResult);

            var queriesDepois = payload.getQueriesPosPipeline()
                .stream()
                .map(QueuePayloadQuery::getQuery)
                .collect(Collectors.toSet());
            ambienteService.runQuery(queriesDepois, ambiente.acessoBanco());
        }
        catch(Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            pipelineResult.setErro(true);
            pipelineResult.setMensagemErro(e.getMessage());
        }
        return pipelineResult;
    }

    private ThreadPoolTaskExecutor novoTaskExecutor(long ambienteId) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);  // Mínimo número de threads na pool
        executor.setMaxPoolSize(1);  // Máximo número de threads na pool
//        executor.setQueueCapacity(20);  // Capacidade da fila de tarefas por thread
        executor.setKeepAliveSeconds(60);  // Mantenha tópicos ociosos por 60 segundos
        executor.setThreadNamePrefix("Task-Ambiente-Id-00" + ambienteId); // Nome da thread
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());  // ?
        executor.setDaemon(true); // Se as threads devem ser interrompidas se o App fechar
        executor.initialize(); // Iniciazar
        return executor;
    }

}
