package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.job.JobController;
import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.relatorio.RelatorioInfoDTO;
import br.com.ppw.dma.relatorio.RelatorioService;
import br.com.ppw.dma.util.ComandoSql;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.LINHA_HORINZONTAL;

@Slf4j
@RestController
@RequestMapping("pipeline")
public class PipelineController extends MasterController<
    Long, Pipeline, PipelineNovaDTO, PipelineInfoDTO, PipelineController> {

    private final PipelineService pipelineService;
    private final JobController jobController;
    private final RelatorioService relatorioService;

    public PipelineController(
        @Autowired PipelineService pipelineService,
        @Autowired JobController jobController,
        @Autowired RelatorioService relatorioService) {
        //--------------------------------------------
        super(pipelineService);
        this.pipelineService = pipelineService;
        this.jobController = jobController;
        this.relatorioService = relatorioService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @PostMapping(value = "new")
    public ResponseEntity<?> newPipeline(@RequestBody PipelineNovaDTO novaPipeline) {
        val serviceId = "$" + Instant.now().toEpochMilli();
        log.info("Iniciando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);

        val pipeline = proxy().newPipelineCore(novaPipeline);

        log.info("Encerrando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        val mensgem = "Pipeline '" +pipeline.getNome()+ "' criada com sucesso.";
        return ResponseEntity.ok(mensgem);
    }

    @PostMapping(value = "run/new")
    public ResponseEntity<?> runNewPipeline(@RequestBody PipelineNovaExecDTO novaPipeline) {
        val serviceId = "$" + Instant.now().toEpochMilli();
        log.info("Iniciando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);

        val pipeline = proxy().newPipelineCore(novaPipeline);
        val evidencias = jobController.executeJobsCore(novaPipeline.getJobs());
        val parametrosDosJobs = novaPipeline.getJobs()
            .stream()
            .map(JobExecuteDTO::getArgumentos)
            .collect(Collectors.joining(" "));
        val relatorio = relatorioService.buildAndPersist(
            novaPipeline.getRelatorio(),
            pipeline,
            evidencias,
            parametrosDosJobs);

        log.info("Encerrando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        val mensgem = "";
        return ResponseEntity.ok(mensgem);
    }

    @PostMapping(value = "run/again")
    public ResponseEntity<?> runPipelineAgain(@RequestBody @NotNull String nome) {
        val serviceId = "$" + Instant.now().toEpochMilli();
        log.info("Iniciando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);

        log.info("Etapa 1: remontando última execução da Pipeline '{}'.", nome);
        val pipeline = pipelineService.getPipelineByName(nome);
        val relatorio = relatorioService.findMostRecentFromPipeline(pipeline);
        val jobsExecDto = relatorio.getEvidencias()
            .stream()
            .map(ev -> {
                val id = ev.getId();
                val comandosSql = ev.getBancoPosJob()
                    .stream()
                    .map(q -> new ComandoSql(q.getTabelaNome()))
                    .toList();
                val comandoSqlString = comandosSql.stream()
                    .map(ComandoSql::getTabela)
                        .collect(Collectors.joining(", "));
                log.info("Tablas usadas pela Evidência ID {}: {}.", id, comandoSqlString);

                val cargas = ev.getCargas()
                    .stream()
                    .map(ExecFile::getArquivo)
                    .toList();
                val cargasString = comandosSql.stream()
                    .map(ComandoSql::getTabela)
                    .collect(Collectors.joining("\n\t"));
                log.info("Cargas usadas nessa Evidência ID {}: \n\t{}", id, cargasString);

                return JobExecuteDTO.builder()
                    .id(ev.getJob().getId())
                    .ordem(ev.getOrdem())
                    .argumentos(ev.getArgumentos())
                    .queries(comandosSql)
                    .cargas(cargas)
                    .build();
            })
            .toList();

        log.info("Total de JobExecDTO's montados: {}.", jobsExecDto.size());
        jobsExecDto.forEach(dto -> log.info(dto.toString()));
        log.info("Remontagem da pipeline finalizada");

        log.info("Etapa 2: reexecutando os Jobs da Pipeline '{}'.", nome);
        val novasEvidencias = jobController.executeJobsCore(jobsExecDto);

        log.info("Etapa 3: salvando novo Relatório para a Pipeline '{}'.", nome);
        val parametrosDosJobs = jobsExecDto.stream()
            .map(JobExecuteDTO::getArgumentos)
            .collect(Collectors.joining(" "));
        val relatorioDto = new RelatorioInfoDTO();
        relatorioDto.setNomeAtividade(relatorio.getNomeAtividade());
        relatorioDto.setNomeProjeto(relatorio.getNomeProjeto());
        relatorioDto.setConfiguracao(relatorioDto.getConfiguracao());
        val novoRelatorio = relatorioService.buildAndPersist(
            relatorioDto,
            pipeline,
            novasEvidencias,
            parametrosDosJobs);

        log.info("Etapa 4: gerando documento DET localmente.");


        log.info("Etapa 5: anexando documento DET na resposta.");
        val arquivoNome = "";
        val diretorio = "";
        val filePath = diretorio + arquivoNome;
        log.debug("Criando objeto Resource, usando o caminho '{}'.", filePath);
        val fileResource = new FileSystemResource(filePath);

        log.debug("Configurando cabeçalho da resposta para arquivo '{}'.", arquivoNome);
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", arquivoNome);

        log.info("Encerrando Serviço {} {}{}", serviceId, LINHA_HORINZONTAL, LINHA_HORINZONTAL);
        val mensgem = "";
        return ResponseEntity.ok()
            .headers(headers)
            .body(fileResource);
    }

    public Pipeline newPipelineCore(PipelineNovaDTO novaPipeline) {
        log.info("Convertendo DTO em Entidade.");
        val pipeline = new Pipeline();
        pipeline.setNome(novaPipeline.getPipeline().getNome());
        pipeline.setDescricao(novaPipeline.getPipeline().getDescricao());
        return pipelineService.persist(pipeline);
    }

    public Pipeline newPipelineCore(PipelineNovaExecDTO novaPipeline) {
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

        log.info("Convertendo Pipeline: de DTO em Entidade.");
        val pipeline = new Pipeline();
        pipeline.setNome(novaPipeline.getPipeline().getNome());
        pipeline.setDescricao(novaPipeline.getPipeline().getDescricao());
        pipeline.setJobs(jobs);
        return pipelineService.persist(pipeline);
    }


}
