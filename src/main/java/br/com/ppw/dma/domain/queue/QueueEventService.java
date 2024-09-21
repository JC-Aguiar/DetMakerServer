package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.evidencia.EvidenciaService;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.pipeline.PipelineService;
import br.com.ppw.dma.domain.queue.result.PipelineResult;
import br.com.ppw.dma.domain.relatorio.RelatorioService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Service
@EnableAsync
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class QueueEventService {

    ObjectMapper objectMapper;
    QueueService queueService;
    JobService jobService;
    EvidenciaService evidenciaService;
    RelatorioService relatorioService;
    PipelineService pipelineService;
    AmbienteService ambienteService;


    @Autowired
    public QueueEventService(
        ObjectMapper objectMapper,
        QueueService queueService,
        JobService jobService,
        EvidenciaService evidenciaService,
        RelatorioService relatorioService,
        PipelineService pipelineService,
        AmbienteService ambienteService) {

        this.queueService = queueService;
        this.objectMapper = objectMapper;
        this.jobService = jobService;
        this.evidenciaService = evidenciaService;
        this.relatorioService = relatorioService;
        this.pipelineService = pipelineService;
        this.ambienteService = ambienteService;
    }


    @Async @EventListener
//    @Transactional(noRollbackFor = Throwable.class)
    public void processQueue(@NonNull Queue itemFila)
    throws DuplicatedRecordException, JsonProcessingException {

        log.info("Desserializando Json em classe Java.");
        var payload = objectMapper.readValue(itemFila.getPayload(), QueuePayload.class);
        log.info(payload.toString());

        itemFila.setPayloadObj(payload);
        itemFila.setStatus(QueueStatus.EXECUTANDO);
        itemFila.setDataExecucao(OffsetDateTime.now(RELOGIO));
        queueService.saveAndFlush(itemFila); //TODO: fluxsh ainda necessário?

        var pipelineResult = runQueue(itemFila);
        queueService.deleteAndFlush(itemFila); //TODO: flush ainda necessário?

        val evidenciasResult = evidenciaService.gerarEvidencia(pipelineResult);
        pipelineResult.addEvidenciaResult(evidenciasResult);

//        report(itemFila.getAmbiente(), pipelineResult);

//        var pipeline = pipelineService.getUniqueOne(pipelineNome, cliente.getId())
//            .orElseThrow(() -> new IllegalStateException("Sem pipeline informada para gerar Evidência."));

        relatorioService.buildAndPersist(itemFila.getAmbiente(), pipelineResult);
    }

//    @Transactional(noRollbackFor = Throwable.class)
    private PipelineResult runQueue(@NonNull Queue itemFila) {

        var ticket = itemFila.getTicket();
        var ambiente = itemFila.getAmbiente();
        var payload = itemFila.getPayloadObj();
        var usuario = itemFila.getUsuario();
        var cliente  = itemFila.getAmbiente().getCliente();

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
            ambienteService.runQuery(queriesAntes, ambiente.acessoBanco());

            pipelineResult.addJobResult(
                jobService.executar(
                    ambiente.acessoBanco(),
                    ambiente.acessoFtp(),
                    payload.getJobs()
            ));
            var queriesDepois = payload.getQueriesPosPipeline()
                .stream()
                .map(QueuePayloadQuery::getQuery)
                .collect(Collectors.toSet());
            ambienteService.runQuery(queriesDepois, ambiente.acessoBanco());
        }
        catch(Exception e) {
            log.error(e.getMessage());
            pipelineResult.setErro(true);
            pipelineResult.setMensagemErro(e.getMessage());
        }
        return pipelineResult;
    }

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
