package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.job.JobPreparation;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.pipeline.Pipeline;
import br.com.ppw.dma.pipeline.PipelineController;
import br.com.ppw.dma.pipeline.PipelinePreparation;
import br.com.ppw.dma.system.FileSystemService;
import br.com.ppw.dma.user.UserInfoDTO;
import br.com.ppw.dma.util.DetHtml;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("relatorio")
public class RelatorioController extends MasterController<Long, Relatorio, RelatorioController> {

    @Autowired
    private ResourceLoader resourceLoader;

    private RelatorioService relatorioService;

    private AmbienteService ambienteService;

    private PipelineController pipelineController;

    private FileSystemService fileSystemService;


    public RelatorioController(
        @Autowired RelatorioService relatorioService,
        @Autowired AmbienteService ambienteService,
        @Autowired PipelineController pipelineController,
        @Autowired FileSystemService fileSystemService){
        //--------------------------------------------------
        super(relatorioService);
        this.relatorioService = relatorioService;
        this.ambienteService = ambienteService;
        this.pipelineController = pipelineController;
        this.fileSystemService = fileSystemService;
    }

    @Override
    public ResponseEntity<RelatorioResumoDTO> parseOne(Relatorio entity) {
        val dto = RelatorioResumoDTO.converterEntidade(entity);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<?> parseAll(Page<Relatorio> relatorios) {
        val dtos = relatorios.map(RelatorioHistoricoDTO::new);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("ambiente/{ambienteId}")
    public ResponseEntity<List<RelatorioResumoDTO>> getAll(@PathVariable(name = "ambienteId") Long ambienteId) {
        final List<RelatorioResumoDTO> dtos = relatorioService.findAllByAmbiente(ambienteId)
            .stream()
            .map(RelatorioResumoDTO::converterEntidade)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("summary")
    public ResponseEntity<Page<RelatorioResumoDTO>> getAllSummarized(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens) {
        //--------------------------------------------------------------
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        val dtos = relatorioService.findAll(pageConfig)
            .map(RelatorioResumoDTO::converterEntidade);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("summary/ambiente/{ambienteId}")
    public ResponseEntity<Page<RelatorioResumoDTO>> getAllSummarized(
        @PathVariable(name = "ambienteId") Long ambienteId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens,
        @RequestParam(name = "idProjeto") Optional<String> idProjeto,
        @RequestParam(name = "nomeProjeto") Optional<String> nomeProjeto,
        @RequestParam(name = "nomeAtividade") Optional<String> nomeAtividade,
        @RequestParam(name = "nomePipeline") Optional<String> nomePipeline,
        @RequestParam(name = "data") Optional<String> dataExecString) {
        //--------------------------------------------------------------
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());

        LocalDate dataExec = null;
        if(dataExecString.isPresent() && !dataExecString.get().isBlank())
            dataExec = LocalDate.parse(dataExecString.get());

        var ambiente = ambienteService.findById(ambienteId);
        var pipeline = new Pipeline();
        pipeline.setNome(nomePipeline.orElse(null));
        var relatorioBusca = Relatorio.builder()
            .idProjeto(idProjeto.orElse(null))
            .nomeProjeto(nomeProjeto.orElse(null))
            .nomeAtividade(nomeAtividade.orElse(null))
            .ambiente(ambiente)
            .data(dataExec)
            .pipeline(pipeline)
            .build();
        Example<Relatorio> exemplo = Example.of(relatorioBusca, MATCHER_ALL);

        Page<RelatorioResumoDTO> relatorios = relatorioService.findAllByExample(exemplo, pageConfig)
            .map(RelatorioResumoDTO::converterEntidade);

        return ResponseEntity.ok(relatorios);
    }

    //TODO:
    @Transactional
    @PostMapping(value = "review")
    public ResponseEntity<Void> salvarRevisao(@NonNull @RequestBody RelatorioRevisadoDTO dto){
        relatorioService.salvarRelatorioRevisado(dto);
        return ResponseEntity.ok(null);
    }

    //TODO: javadoc
    @GetMapping(value = "review/{id}")
    public ResponseEntity<?> revisarUmRelatorio(@PathVariable long id) {
        val relatorio = relatorioService.findById(id);
        log.info("Relatório encontrado:");
        log.info(relatorio.toString());

        val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
        return ResponseEntity.ok(relatorioHistorico);
    }

    //TODO: javadoc
    @GetMapping(value = "det/{id}")
    public ResponseEntity<?> obterDet(
        @PathVariable long id,
        @RequestParam(name = "user", required = true) String user,
        @RequestParam(name = "email", required = true) String email)
    throws IOException, URISyntaxException {
        val relatorio = relatorioService.findById(id);
        log.info("Relatório encontrado:");
        log.info(relatorio.toString());

        //Validando se o Relatório está pronto para gerar documento DET
        val podeGerarDet = relatorio.getEvidencias()
            .stream()
            .allMatch(Evidencia::jaRevisada);
        if(!podeGerarDet) return ResponseEntity.badRequest()
            .body("O Relatório ID " +id+ " não está totalmente revisado para gerar DET");

        //TODO: o usuário tem que ser quem fez a solicitação de execução da pipleine/relatório
        val detUser = new UserInfoDTO();
        detUser.setNome(user);
        detUser.setEmail(email);
        detUser.setPapel("DEV");
        detUser.setEmpresa("PPW");
        return retornarNovoDet(DetDTO.from(relatorio, List.of(detUser)));
    }

    //TODO: javadoc
    //TODO: aplicar @Synchronized e testar.
    //  Se funcionar, o timeout do front precisa ser atualizado com base na quantidade de espera na fila
    @Transactional
    @GetMapping(value = "rerun/{id}")
    public ResponseEntity<RelatorioHistoricoDTO> runAgain(@PathVariable long id) {
        val relatorio = relatorioService.findById(id);
        val pipeline = relatorio.getPipeline();
        val ambiente = relatorio.getAmbiente();
        val jobs = relatorio.getEvidencias()
            .stream()
            .map(ev -> {
                log.info("Recriando as propriedades executadas na Evidência ID {}", ev.getId());
                return new JobPreparation(ev.getJob(), new JobExecuteDTO(ev));
//                val process = JobPreparation(ev);
//                log.info("Convertendo registro ExecFile em arquivos temporários para envio SFTP.");
//                val cargas = ev.getCargas()
//                    .stream()
//                    .map(fileSystemService::store)
//                    .toList();
//                process.setCargas(cargas);
//                log.info(process.toString());
//                return process;
            })
            .toList();
        return pipelineController.run(new PipelinePreparation(
            pipeline,
            new AtividadeInfoDTO(relatorio),
            ambiente,
            jobs,
            Map.of(),
            Map.of()
        ));
    }

    private ResponseEntity<Resource> retornarNovoDet(@NonNull DetDTO pipelineRelatorio)
    throws IOException, URISyntaxException {
        //log.debug("Criando objeto Resource para arquivo no caminho '{}'.", det.getAbsolutePath());
        val det = new DetHtml(resourceLoader, pipelineRelatorio);
        final Resource fileResource = new ByteArrayResource(det.getDocumento());

        log.debug("Configurando cabeçalho da resposta: 'attachment/{}'.", det.getDocumentoNome());
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", det.getDocumentoNome());

        // Retorna a resposta com o arquivo
        return ResponseEntity.ok()
            .headers(headers)
            .body(fileResource);
    }
}
