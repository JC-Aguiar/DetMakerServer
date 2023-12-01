package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.evidencia.EvidenciaController;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.pipeline.DetDTO;
import br.com.ppw.dma.user.UserInfoDTO;
import br.com.ppw.dma.util.DetHtml;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class RelatorioController extends MasterController<
    Long, Relatorio, MasterRequestDTO, RelatorioHistoricoDTO, RelatorioController> {

    private final RelatorioService relatorioService;
    private final EvidenciaController evidenciaController;
    private final ResourceLoader resourceLoader;

    public RelatorioController(
        @Autowired RelatorioService relatorioService,
        @Autowired EvidenciaController evidenciaController,
        @Autowired ResourceLoader resourceLoader){
        //---------------------------------------------
        super(relatorioService);
        this.relatorioService = relatorioService;
        this.evidenciaController = evidenciaController;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Override
    public ResponseEntity<RelatorioHistoricoDTO> getOne(@NonNull Long id) throws NoSuchMethodException {
        val dto = new RelatorioHistoricoDTO(relatorioService.findById(id));
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<Page<RelatorioHistoricoDTO>> getAll(int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        val dtos = relatorioService.findAll(pageConfig)
            .map(RelatorioHistoricoDTO::new);
        return ResponseEntity.ok(dtos);
    }

    //TODO:
    @Transactional
    @PostMapping(value = "save/review")
    public ResponseEntity<Void> salvarRevisao(@NonNull @RequestBody RelatorioRevisadoDTO dto){
        relatorioService.salvarRelatorioRevisado(dto);
        return ResponseEntity.ok(null);
    }

    //TODO: javadoc
    @GetMapping(value = "det/{id}")
    public ResponseEntity<Resource> obterDet(@PathVariable long id)
    throws IOException, URISyntaxException {
        val relatorio = relatorioService.findById(id);
        log.info("Relatório encontrado:");
        log.info(relatorio.toString());

        //TODO: ajustar
        val det = DetDTO.from(relatorio);
        val detUser = new UserInfoDTO();
        detUser.setNome("Autopilot DetMaker User");
        detUser.setPapel("IA");
        detUser.setEmpresa("PPW");

        return retornarNovoDet(det, List.of(detUser));
    }

    private ResponseEntity<Resource> retornarNovoDet(
        @NonNull DetDTO pipelineRelatorio,
        @NonNull List<UserInfoDTO> userInfo)
    throws IOException, URISyntaxException {
        val det = new DetHtml(resourceLoader, pipelineRelatorio, userInfo).getDocumento();

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
}
