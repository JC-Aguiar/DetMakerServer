package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.job.JobController;
import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.relatorio.RelatorioService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("pipeline")
public class PipelineController extends MasterController<
    Long, Pipeline, MasterRequestDTO, PipelineInfoDTO, PipelineController> {

    private final PipelineService pipelineService;
    private final JobController jobController;
    private final RelatorioService relatorioService;
    private final EvidenciaService evidenciaService;

    public PipelineController(
        @Autowired PipelineService pipelineService,
        @Autowired JobController jobController,
        @Autowired RelatorioService relatorioService,
        @Autowired EvidenciaService evidenciaService) {
        //--------------------------------------------
        super(pipelineService);
        this.pipelineService = pipelineService;
        this.jobController = jobController;
        this.relatorioService = relatorioService;
        this.evidenciaService = evidenciaService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }


    @PostMapping(value = "run")
    public ResponseEntity<RelatorioHistoricoDTO> runNewPipeline(
        @NonNull @RequestBody PipelineNovaExecDTO novaPipeline) {
        //-------------------------------------------------------
        //Coleta ou cria Pipeline
        val pipeline = getAndUpdate(novaPipeline.getPipeline())
            .orElseGet(() -> proxy().createNewByPipelineNovaExecDTO(novaPipeline));

        //Executa os Jobs e obtêm as evidências
        val evidencias = jobController.executeJobsAndGetEvidencias(novaPipeline.getJobs());

        //Coleta todos os parâmetros de entrada dos Jobs
        val parametrosDosJobs = evidencias.stream()
            .map(ev -> ev.getJob().getNome() +" "+ ev.getArgumentos())
            .collect(Collectors.joining("\n"));

        //Cria e salva novo relatório
        val relatorio = relatorioService.buildAndPersist(
            novaPipeline.getRelatorio(),
            pipeline,
            evidencias,
            parametrosDosJobs);

        log.info("Montando RelatorioHistoricoDTO.");
        val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
        log.info(relatorioHistorico.toString());

        //log.info("Anexando RelatorioHistoricoDTO no DetDTO.");
        //val pipelineRelatorio = new DetDTO(
        //    novaPipeline.getPipeline().getNome(),
        //    novaPipeline.getPipeline().getDescricao(),
        //    relatorioHistorico);
        //log.info(pipelineRelatorio.toString());

        return ResponseEntity.ok(relatorioHistorico);
        //return proxy().retornarNovoDet(pipelineRelatorio, List.of(novaPipeline.getUserInfo()));
    }



    public JobExecuteDTO evidenciaParaJobExecuteDto(@NonNull Evidencia evidencia) {
        val id = evidencia.getId();
        val comandosSql = evidencia.getBanco()
            .stream()
            .map(q -> new ComandoSql(q.getTabelaNome()))
            .toList();
        val comandoSqlString = comandosSql.stream()
            .map(ComandoSql::getTabela)
            .collect(Collectors.joining(", "));
        log.info("Tablas usadas pela Evidência ID {}: {}.", id, comandoSqlString);

        val cargas = evidencia.getCargas()
            .stream()
            .map(ExecFile::getArquivo)
            .toList();
        log.info("Cargas usadas na Evidência ID {}:", id);
        cargas.forEach(c -> log.info(" - {}", c));

        return JobExecuteDTO.builder()
            .id(evidencia.getJob().getId())
            .ordem(evidencia.getOrdem())
            .argumentos(evidencia.getArgumentos())
            .queries(comandosSql)
            .cargas(cargas)
            .build();
    }

    public Pipeline createNewByPipelineNovaExecDTO(PipelineNovaExecDTO novaPipeline) {
        log.info("Criando nova Pipeline '{}'.", novaPipeline.getPipeline().getNome());
        log.info("Obtendo todos os Jobs listados no DTO.");
        val jobIds = novaPipeline.getJobs()
            .stream()
            .map(JobExecuteDTO::getId)
            .toList();
        val jobs = (List<Job>) jobController.getService().findAllById(jobIds);
        val jobsNome = jobs.stream()
            .map(Job::getNome)
            .collect(Collectors.joining(", "));
        log.info("Total de {} Jobs encontrados:", jobs.size());
        log.info(" - {}", jobsNome);

        val pipeline = pipelineService.parsePipelineNovaExecDTO(novaPipeline, jobs);
        return pipelineService.persist(pipeline);
    }

    public Optional<Pipeline> getAndUpdate(PipelineInfoDTO pipelineDto) {
        val pipeline = pipelineService.getByName(pipelineDto.getNome());
        if(pipeline.isEmpty()) return pipeline;

        val pipelineBanco = pipeline.get();
        log.info("Comparando Pipelines.");
        log.info("Pipelines Usuário: {}", pipelineDto);
        log.info("Pipelines Banco: {}", pipelineBanco);
        val novaDescricao = pipelineBanco.atualizarDescricao(pipelineDto.getDescricao());
        val novosJobs = pipelineBanco.atualizarJobs(pipelineDto.getJobs());

        if(novaDescricao || novosJobs) {
            log.info("A Pipeline precisa ser atualizada.");
            if(novaDescricao)
                pipelineBanco.setDescricao(pipelineDto.getDescricao());
            if(novosJobs)
                pipelineBanco.setJobs(jobController.getAllByNomes(pipelineDto.getJobs()));
            pipelineService.persist(pipelineBanco);
        }
        return pipeline;
    }

}
