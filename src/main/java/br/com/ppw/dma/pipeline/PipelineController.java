package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.job.JobController;
import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.relatorio.RelatorioInfoDTO;
import br.com.ppw.dma.relatorio.RelatorioService;
import br.com.ppw.dma.user.UserInfoDTO;
import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.HtmlDet;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.LINHA_HIFENS;

@Slf4j
@RestController
@RequestMapping("pipeline")
public class PipelineController extends MasterController<
    Long, Pipeline, PipelineNovaDTO, PipelineInfoDTO, PipelineController> {

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

    @PostMapping(value = "new")
    public ResponseEntity<?> newPipeline(@RequestBody PipelineNovaDTO novaPipeline) {
        val pipeline = proxy().createNewByPipelineNovaDTO(novaPipeline);
        val mensgem = "Pipeline '" +pipeline.getNome()+ "' criada com sucesso.";
        return ResponseEntity.ok(mensgem);
    }

    @PostMapping(value = "run/new")
    public ResponseEntity<?> runNewPipeline(@RequestBody PipelineNovaExecDTO novaPipeline)
    throws IOException, URISyntaxException {
        val pipeline = pipelineService
            .getPipelineByName(novaPipeline.getPipeline().getNome())
            .orElseGet(() -> proxy().createNewByPipelineNovaExecDTO(novaPipeline));
        val evidencias = jobController.executeJobsAndGetEvidencias(novaPipeline.getJobs());
        val parametrosDosJobs = novaPipeline.getJobs()
            .stream()
            .map(JobExecuteDTO::getArgumentos)
            .collect(Collectors.joining(" "));
        val relatorio = relatorioService.buildAndPersist(
            novaPipeline.getRelatorio(),
            pipeline,
            evidencias,
            parametrosDosJobs);
        val evidenciasDto = evidencias.stream()
            .map(ev -> evidenciaService.parseToResponseDto(ev, ev.getOrdem()))
            .toList();
        val dataInicio = evidenciasDto.stream()
            .map(EvidenciaInfoDTO::getData)
            .min(OffsetDateTime::compareTo)
            .orElse(null);
        val dataFim = evidenciasDto.stream()
            .map(EvidenciaInfoDTO::getData)
            .max(OffsetDateTime::compareTo)
            .orElse(null);
        val sucesso = evidenciasDto.stream()
            .allMatch(EvidenciaInfoDTO::getSucesso);

        log.info("Montando RelatorioHistoricoDTO.");
        val relatorioHistorico = RelatorioHistoricoDTO.builder()
            .nomeAtividade(relatorio.getNomeAtividade())
            .nomeProjeto(relatorio.getNomeProjeto())
            .configuracao(relatorio.getConfiguracao())
            .evidencias(evidenciasDto)
            .dataInicio(dataInicio)
            .dataFim(dataFim)
            .sucesso(sucesso)
            .build();
        log.info(relatorioHistorico.toString());

        log.info("Montando PipelineRelatorioDTO.");
        val pipelineRelatorio = new PipelineRelatorioDTO(
            novaPipeline.getPipeline().getNome(),
            novaPipeline.getPipeline().getDescricao(),
            relatorioHistorico);
        log.info(pipelineRelatorio.toString());

        return retornarNovoDet(pipelineRelatorio, List.of(novaPipeline.getUserInfo()));
    }

    private ResponseEntity<?> retornarNovoDet(
            @NonNull PipelineRelatorioDTO pipelineRelatorio,
            @NonNull List<UserInfoDTO> userInfo)
    throws IOException, URISyntaxException {
        val det = HtmlDet.gerarNovoDet(pipelineRelatorio, userInfo);
        val mensgem = "Pipeline executada com sucesso. Novo DET foi gerado e salvo localmente.";

        log.debug("Criando objeto Resource para arquivo no caminho '{}'.", det.getAbsolutePath());
        final Resource fileResource = new FileSystemResource(det.getAbsolutePath());

        log.debug("Configurando cabeçalho da resposta, usando propriedade 'attachment/{}'.", det.getName());
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", det.getName());

        // Retorna a resposta com o arquivo
        return ResponseEntity.ok()
            .headers(headers)
            .body(fileResource);
    }

    @GetMapping(value = "run/again/{nome}")
    public ResponseEntity<?> runPipelineAgain(
        @PathVariable @NotBlank String nome)
    throws IOException, URISyntaxException {
        log.info("Etapa 1: remontando última execução da Pipeline '{}'.", nome);
        val pipeline = pipelineService.getPipelineByName(nome)
            .orElseThrow();
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

        log.info(LINHA_HIFENS);
        log.info("Etapa 2: reexecutando os Jobs da Pipeline '{}'.", nome);
        val novasEvidencias = jobController.executeJobsAndGetEvidencias(jobsExecDto);

        log.info(LINHA_HIFENS);
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

        log.info(LINHA_HIFENS);
        log.info("Etapa 4: coletando informações para montar DET.");
        val evidenciasDto = novasEvidencias.stream()
            .map(ev -> evidenciaService.parseToResponseDto(ev, ev.getOrdem()))
            .toList();
        val dataInicio = evidenciasDto.stream()
            .map(EvidenciaInfoDTO::getData)
            .min(OffsetDateTime::compareTo)
            .orElse(null);
        val dataFim = evidenciasDto.stream()
            .map(EvidenciaInfoDTO::getData)
            .max(OffsetDateTime::compareTo)
            .orElse(null);
        val sucesso = evidenciasDto.stream()
            .allMatch(EvidenciaInfoDTO::getSucesso);
        val relatorioHistorico = RelatorioHistoricoDTO.builder()
            .nomeAtividade(relatorio.getNomeAtividade())
            .nomeProjeto(relatorio.getNomeProjeto())
            .configuracao(relatorio.getConfiguracao())
            .evidencias(evidenciasDto)
            .dataInicio(dataInicio)
            .dataFim(dataFim)
            .sucesso(sucesso)
            .build();
        val pipelineRelatorio = new PipelineRelatorioDTO(
            pipeline.getNome(),
            pipeline.getDescricao(),
            relatorioHistorico);

        log.info(LINHA_HIFENS);
        log.info("Etapa 5: gerando documento DET localmente.");
        return retornarNovoDet(
            pipelineRelatorio, List.of(new UserInfoDTO())); //TODO: implementar parâmetro userInfoDTO
    }

    public Pipeline createNewByPipelineNovaDTO(PipelineNovaDTO novaPipeline) {
        log.info("Convertendo DTO em Entidade.");
        val pipeline = new Pipeline();
        pipeline.setNome(novaPipeline.getPipeline().getNome());
        pipeline.setDescricao(novaPipeline.getPipeline().getDescricao());
        return pipelineService.persist(pipeline);
    }

    public Pipeline createNewByPipelineNovaExecDTO(PipelineNovaExecDTO novaPipeline) {
        log.info("Criando nova Pipeline '{}'.", novaPipeline);
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


}
