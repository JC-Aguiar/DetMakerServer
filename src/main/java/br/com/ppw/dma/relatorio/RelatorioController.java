package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.evidencia.EvidenciaController;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.pipeline.PipelineService;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("relatorio")
public class RelatorioController extends MasterController<Long, Relatorio, RelatorioController> {

    @Autowired
    private ResourceLoader resourceLoader;

    private RelatorioService relatorioService;

    private PipelineService pipelineService;

    private EvidenciaController evidenciaController;


    public RelatorioController(
        @Autowired RelatorioService relatorioService,
        @Autowired PipelineService pipelineService,
        @Autowired EvidenciaController evidenciaController){
        //--------------------------------------------------
        super(relatorioService);
        this.relatorioService = relatorioService;
        this.pipelineService = pipelineService;
        this.evidenciaController = evidenciaController;
    }

    @Override
    public ResponseEntity<?> parseOne(Relatorio entity) {
        val dto = new RelatorioHistoricoDTO(entity);
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
        @RequestParam(name = "itens", defaultValue = "12") int itens) {
        //--------------------------------------------------------------
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        val dtos = relatorioService
            .findAllByAmbiente(pageConfig, ambienteId)
            .map(RelatorioResumoDTO::converterEntidade);
        return ResponseEntity.ok(dtos);
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

        //TODO: ajustar
        val det = DetDTO.from(relatorio);
        val detUser = new UserInfoDTO();
        detUser.setNome(user);
        detUser.setEmail(email);
        detUser.setPapel("DEV");
        detUser.setEmpresa("PPW");

        return retornarNovoDet(det, List.of(detUser));
    }

    private ResponseEntity<Resource> retornarNovoDet(
        @NonNull DetDTO pipelineRelatorio,
        @NonNull List<UserInfoDTO> userInfo)
    throws IOException, URISyntaxException {

        //log.debug("Criando objeto Resource para arquivo no caminho '{}'.", det.getAbsolutePath());
        val det = new DetHtml(resourceLoader, pipelineRelatorio, userInfo);
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
