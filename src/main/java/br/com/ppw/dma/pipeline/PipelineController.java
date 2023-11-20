package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.job.JobController;
import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.relatorio.RelatorioInfoDTO;
import br.com.ppw.dma.relatorio.RelatorioService;
import br.com.ppw.dma.user.UserInfoDTO;
import br.com.ppw.dma.util.DetHtml;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.LINHA_HIFENS;

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


    @PostMapping(value = "run/new")
    public ResponseEntity<?> runNewPipeline(@NonNull @RequestBody PipelineNovaExecDTO novaPipeline)
    throws IOException, URISyntaxException {
        //Coleta ou cria Pipeline
        val pipeline = getAndUpdate(novaPipeline.getPipeline())
            .orElseGet(() -> proxy().createNewByPipelineNovaExecDTO(novaPipeline));

        //Executa os Jobs e obtêm as evidências
        val evidencias = jobController.executeJobsAndGetEvidencias(novaPipeline.getJobs());

        //Coleta todos os parâmetros de entrada dos Jobs
        val parametrosDosJobs = novaPipeline.getJobs()
            .stream()
            .map(JobExecuteDTO::getArgumentos)
            .collect(Collectors.joining(" "));

        //Cria e salva novo relatório
        val relatorio = relatorioService.buildAndPersist(
            novaPipeline.getRelatorio(),
            pipeline,
            evidencias,
            parametrosDosJobs);

        //Convertendo Evidencia para DTO
        val evidenciasDto = evidencias.stream()
            .map(ev -> evidenciaService.parseToResponseDto(ev, ev.getOrdem()))
            .toList();

        //Obtendo demais dados (hora e status)
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

        log.info("Anexando RelatorioHistoricoDTO no PipelineRelatorioDTO.");
        val pipelineRelatorio = new PipelineRelatorioDTO(
            novaPipeline.getPipeline().getNome(),
            novaPipeline.getPipeline().getDescricao(),
            relatorioHistorico);
        log.info(pipelineRelatorio.toString());

        return proxy().retornarNovoDet(pipelineRelatorio, List.of(novaPipeline.getUserInfo()));
    }

    private ResponseEntity<?> retornarNovoDet(
        @NonNull PipelineRelatorioDTO pipelineRelatorio,
        @NonNull List<UserInfoDTO> userInfo)
    throws IOException, URISyntaxException {
        val det = new DetHtml(pipelineRelatorio, userInfo).getDocumento();

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
        val pipeline = pipelineService.getByName(nome)
            .orElseThrow();
        val relatorio = relatorioService.findMostRecentFromPipeline(pipeline);
        val jobsExecDto = relatorio.getEvidencias()
            .stream()
            .map(this::evidenciaParaJobExecuteDto)
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
        relatorioService.buildAndPersist(
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
        log.info("Etapa 5: gerando documento DET.");
        return retornarNovoDet(
            pipelineRelatorio, List.of(new UserInfoDTO())); //TODO: implementar parâmetro userInfoDTO
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

    public Optional<Pipeline> getAndUpdate(PipelineInfoDTO pipelineInfo) {
        val pipeline = pipelineService.getByName(pipelineInfo.getNome());
        if(pipeline.isEmpty()) return pipeline;

        log.info("Validando se a Pipeline precisa ser atualizada.");
        val pipelineInfoBanco = pipeline.get();
        val novaDescricao = pipelineInfoBanco.atualizarDescricao(pipelineInfo);
        val novosJobs = pipelineInfoBanco.atualizarJobs(pipelineInfo);

        if(novaDescricao || novosJobs) {
            log.info("A Pipeline precisa ser atualizada.");
            if(novaDescricao)
                pipelineInfoBanco.setDescricao(pipelineInfo.getDescricao());
            if(novosJobs)
                pipelineInfoBanco.setJobs(jobController.getAllByNomes(pipelineInfo.getJobs()));
            pipelineService.persist(pipelineInfoBanco);
        }
        return pipeline;
    }

}
