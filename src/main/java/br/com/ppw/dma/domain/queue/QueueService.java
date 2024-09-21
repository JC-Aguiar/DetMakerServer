package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.evidencia.EvidenciaService;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.pipeline.PipelineService;
import br.com.ppw.dma.domain.relatorio.RelatorioService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Slf4j
@Service
@EnableAsync
public class QueueService extends MasterService<Long, Queue, QueueService> {

    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final QueueRepository queueDao;
    private final JobService jobService;
    private final EvidenciaService evidenciaService;
    private final RelatorioService relatorioService;
    private final PipelineService pipelineService;
    private final AmbienteService ambienteService;

    @PersistenceContext
    private EntityManager entityManager;
//    private final JobService jobService;
//    private final EvidenciaService evidenciaService;
//    private final RelatorioService relatorioService;
//    private final PipelineService pipelineService;



    @Autowired
    public QueueService(
        ApplicationEventPublisher publisher,
        ObjectMapper objectMapper,
        QueueRepository queueDao,
        JobService jobService,
        EvidenciaService evidenciaService,
        RelatorioService relatorioService,
        PipelineService pipelineService,
        AmbienteService ambienteService) {

        super(queueDao);
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.queueDao = queueDao;
        this.jobService = jobService;
        this.evidenciaService = evidenciaService;
        this.relatorioService = relatorioService;
        this.pipelineService = pipelineService;
        this.ambienteService = ambienteService;
    }

    @Transactional
    public Queue persist(@NotNull Queue queue) {
        log.info("Persistindo Queue no banco:");
        log.info(queue.toString());
        queue = queueDao.save(queue);

        log.info("Evidência ID {} gravado com sucesso.", queue.getId());
        return queue;
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
    public QueuePushResponseDTO pushQueueItem(
        @NonNull Ambiente ambiente,
        @NonNull String usuario,
        @NonNull QueuePayload payload)
    throws JsonProcessingException, DuplicatedRecordException {
//        var itensExecutando = countByStatusInAmbiente(ambiente, EXECUTANDO);
//        var queueSize = countByStatusInAmbiente(ambiente, AGUARDANDO);
        var queueSize = countInAmbiente(ambiente);
        if(queueSize > 0) {
            return QueuePushResponseDTO.blocked(queueSize);
        }
        var ticket = UUID.randomUUID().toString();
        log.info("Ticket desta solicitação: {}.", ticket);

        var json = objectMapper.writeValueAsString(payload);
        var itemFila = Queue.builder()
            .ticket(ticket)
            .ambiente(ambiente)
            .pipeline(payload.getPipelineNome())
            .usuario(usuario)
            .payload(json)
            .dataSolicitacao(OffsetDateTime.now(RELOGIO))
            .status(QueueStatus.AGUARDANDO)
            .build();
        itemFila = save(itemFila);
        publisher.publishEvent(itemFila);

        return new QueuePushResponseDTO(ticket, queueSize);
    }


//    @Async
//    @EventListener
//    public void pushQueueEvent(@NonNull Queue itemFila)
//    throws JsonProcessingException, DuplicatedRecordException {
//        itemFila.setStatus(QueueStatus.EXECUTANDO);
//        itemFila.setDataExecucao(OffsetDateTime.now(RELOGIO));
//        save(itemFila);
//
//        try {
//            log.info("Desserializando Json em classe Java.");
//            var payload = objectMapper.readValue(itemFila.getPayload(), QueuePayload.class);
//            log.info(payload.toString());
//
//            var ambiente = itemFila.getAmbiente();
//            var cliente = itemFila.getAmbiente().getCliente();
//
//            val jobsProcessados = jobService.executar(
//                ambiente.acessoBanco(),
//                ambiente.acessoFtp(),
//                payload.getJobs()
//            );
//
//            val evidencias = evidenciaService.gerarEvidencia(jobsProcessados);
//            var pipeline = pipelineService
//                .getUniqueOne(payload.getPipelineNome(), cliente.getId())
//                .orElseThrow();
//            val relatorio = relatorioService.buildAndPersist(
//                cliente,
//                ambiente,
//                pipeline,
//                itemFila.getUsuario(),
//                evidencias);
//        }
//        catch(JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        finally {
//            delete(itemFila);
//        }
//        //val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
//    }


    public Queue saveAndFlush(@NonNull Queue entity) throws DuplicatedRecordException {
//        var resultado = save(entity);
//        entityManager.getTransaction().commit();
        return queueDao.saveAndFlush(entity);
//        return resultado;
    }

    public void deleteAndFlush(@NonNull Queue entity) {
        delete(entity);
        queueDao.flush();
    }
//
//    @Async
//    @EventListener
//    @Transactional(noRollbackFor = Throwable.class)
//    public void processQueue(@NonNull Queue itemFila)
//            throws DuplicatedRecordException, JsonProcessingException {
//        log.info("Desserializando Json em classe Java.");
//        var payload = objectMapper.readValue(itemFila.getPayload(), QueuePayload.class);
//        log.info(payload.toString());
//
//        itemFila.setPayloadObj(payload);
//        itemFila.setStatus(QueueStatus.EXECUTANDO);
//        itemFila.setDataExecucao(OffsetDateTime.now(RELOGIO));
//
//        save(itemFila); //TODO: fluxsh ainda necessário?
//        entityManager.getTransaction().commit();
//
//        var pipelineResult = runQueue(itemFila);
//
//        delete(itemFila); //TODO: flush ainda necessário?
//        entityManager.getTransaction().commit();
//
//        report(itemFila.getAmbiente(), pipelineResult);
//    }
//
//    @Transactional(noRollbackFor = Throwable.class)
//    private PipelineResult runQueue(@NonNull Queue itemFila) {
//        var ticket = itemFila.getTicket();
//        var ambiente = itemFila.getAmbiente();
//        var payload = itemFila.getPayloadObj();
//        var usuario = itemFila.getUsuario();
//
//        var pipelineResult = PipelineResult.builder()
//            .pipelineNome(payload.getPipelineNome())
//            .pipelineDescricao(payload.getPipelineDescricao())
//            .ticket(ticket)
//            .usuario(usuario)
//            .build();
//
//        try {
//            var queriesAntes = payload.getQueriesPrePipeline()
//                .stream()
//                .map(QueuePayloadQuery::getQuery)
//                .collect(Collectors.toSet());
//            ambienteService.runQuery(queriesAntes, ambiente.acessoBanco());
//
//            pipelineResult.addJobResult(
//                jobService.executar(
//                    ambiente.acessoBanco(),
//                    ambiente.acessoFtp(),
//                    payload.getJobs()
//                ));
//            var queriesDepois = payload.getQueriesPosPipeline()
//                .stream()
//                .map(QueuePayloadQuery::getQuery)
//                .collect(Collectors.toSet());
//            ambienteService.runQuery(queriesDepois, ambiente.acessoBanco());
//        }
//        catch(Exception e) {
//            log.error(e.getMessage());
//            pipelineResult.setErro(true);
//            pipelineResult.setMensagemErro(e.getMessage());
//        }
//        return pipelineResult;
//    }
//
//    @Transactional(noRollbackFor = Throwable.class)
//    private void report(@NonNull Ambiente ambiente, @NonNull PipelineResult pipelineResult) {
//        var pipelineNome = pipelineResult.getPipelineNome();
//        var jobsProcessados = pipelineResult.getResultadoJobs();
//        var usuario = pipelineResult.getUsuario();
//        var cliente = ambiente.getCliente();
//        var pipeline = pipelineService.getUniqueOne(pipelineNome, cliente.getId())
//            .orElseThrow(() -> new IllegalStateException("Sem pipeline informada para gerar Evidência."));
//
//        val evidencias = evidenciaService.gerarEvidencia(jobsProcessados);
//        relatorioService.buildAndPersist(
//            cliente,
//            ambiente,
//            pipeline,
//            usuario,
//            evidencias
//        );
//    }
}
