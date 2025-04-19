package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.task.TaskPayload;
import br.com.ppw.dma.domain.task.TaskPayloadJob;
import br.com.ppw.dma.domain.task.TaskPushResponseDTO;
import br.com.ppw.dma.domain.task.TaskService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("relatorio")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RelatorioController extends MasterController<Long, Relatorio, RelatorioController> {

    RelatorioService relatorioService;
    AmbienteService ambienteService;
    TaskService taskService;


    @Autowired
    public RelatorioController(
        RelatorioService relatorioService,
        AmbienteService ambienteService,
        TaskService taskService)
    {
        super(relatorioService);
        this.relatorioService = relatorioService;
        this.ambienteService = ambienteService;
        this.taskService = taskService;
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

    //TODO: criar um DTO para receber essa requisição
    @GetMapping("summary/ambiente/{ambienteId}")
    public ResponseEntity<Page<RelatorioResumoDTO>> getAllSummarized(
        @PathVariable(name = "ambienteId") Long ambienteId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens,
        @RequestParam(name = "ticket") Optional<String> ticket,
        @RequestParam(name = "idProjeto") Optional<String> idProjeto,
        @RequestParam(name = "nomeProjeto") Optional<String> nomeProjeto,
        @RequestParam(name = "nomeAtividade") Optional<String> nomeAtividade,
        @RequestParam(name = "nomePipeline") Optional<String> nomePipeline,
        @RequestParam(name = "autor") Optional<String> autor,
        @RequestParam(name = "data") Optional<String> dataExecString) {
        //--------------------------------------------------------------
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());

        LocalDate dataExec = null;
        if(dataExecString.isPresent() && !dataExecString.get().isBlank())
            dataExec = LocalDate.parse(dataExecString.get());

        var ambiente = ambienteService.findById(ambienteId);
        var relatorioBusca = Relatorio.builder()
            .ticket(ticket.orElse(null))
            .idProjeto(idProjeto.orElse(null))
            .nomeProjeto(nomeProjeto.orElse(null))
            .nomeAtividade(nomeAtividade.orElse(null))
            .usuario(autor.orElse(null))
            .ambiente(ambiente)
            .data(dataExec)
            .pipelineNome(nomePipeline.orElse(null))
            .build();
        Example<Relatorio> exemplo = Example.of(relatorioBusca, MATCHER_ALL);

        Page<RelatorioResumoDTO> relatorios = relatorioService.findAllByExample(exemplo, pageConfig)
            .map(RelatorioResumoDTO::converterEntidade);

        return ResponseEntity.ok(relatorios);
    }

    //TODO: javadoc
    @Transactional
    @PostMapping(value = "review")
    public ResponseEntity<RelatorioHistoricoDTO> salvarRevisao(
        @RequestBody RelatorioComplementoDTO dto)
    {
        var id = dto.getId();
        var relatorio = relatorioService.salvarRelatorioRevisado(dto);
        return ResponseEntity.ok(new RelatorioHistoricoDTO(relatorio));
    }

    //TODO: javadoc
    @GetMapping(value = "review/{id}")
    public ResponseEntity<RelatorioHistoricoDTO> revisarUmRelatorio(@PathVariable long id) {
        val relatorio = relatorioService.findById(id);
        log.info("Relatório encontrado:");
        log.info(relatorio.toString());

        val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
        return ResponseEntity.ok(relatorioHistorico);
    }

    //TODO: javadoc
    @Transactional
    @GetMapping(value = "rerun/{id}")
    public ResponseEntity<TaskPushResponseDTO> runAgain(@PathVariable long id)
        throws JsonProcessingException, DuplicatedRecordException {
        val relatorio = relatorioService.findById(id);
        val ambiente = relatorio.getAmbiente();
        val usuario = relatorio.getUsuario();   //TODO: alterar para usuário logado
        val jobs = relatorio.getEvidencias()
            .stream()
            .map(TaskPayloadJob::new)
            .toList();
        var solicitacao = TaskPayload.builder()
            .pipelineNome(relatorio.getPipelineNome())
            .pipelineDescricao(relatorio.getPipelineDescricao())
            .jobs(jobs)
            .build();

        //TODO: o usuário deve ser o que fez a nova solicitação!
        var queueResponse = taskService.pushTaskToQueue(ambiente, usuario, solicitacao);
        if(queueResponse.getQueueSize() > 0)
            return ResponseEntity.unprocessableEntity().body(queueResponse);
        return ResponseEntity.ok(queueResponse);
    }

    //TODO: master o endpoint "det/{id}" comentado abaixo?
//    @GetMapping(value = "det/{id}")
//    public ResponseEntity<?> obterDet(
//        @PathVariable long id,
//        @RequestParam(name = "user", required = true) String user,
//        @RequestParam(name = "email", required = true) String email)
//    throws IOException, URISyntaxException {
//        val relatorio = relatorioService.findById(id);
//        log.info("Relatório encontrado:");
//        log.info(relatorio.toString());
//
//        //Validando se o Relatório está pronto para gerar documento DET
//        if(!relatorio.podeGerarDet()) {
//            return ResponseEntity.badRequest()
//                .body("O Relatório ID " + id + " não está totalmente revisado para gerar DET");
//        }
//        //TODO: o usuário tem que ser quem fez a solicitação de execução da pipleine/relatório
//        val detUser = new UserInfoDTO();
//        detUser.setNome(user);
//        detUser.setEmail(email);
//        detUser.setPapel("DEV");
//        detUser.setEmpresa("PPW");
//        return  retornarNovoDet(DetDTO.from(relatorio, List.of(detUser)));
//    }

//    private ResponseEntity<Resource> retornarNovoDet(@NonNull DetDTO pipelineRelatorio)
//    throws IOException, URISyntaxException {
//        //log.debug("Criando objeto Resource para arquivo no caminho '{}'.", det.getAbsolutePath());
//        val det = new DetHtml(resourceLoader, pipelineRelatorio);
//        final Resource fileResource = new ByteArrayResource(det.getDocumento());
//
//        log.debug("Configurando cabeçalho da resposta: 'attachment/{}'.", det.getDocumentoNome());
//        val headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setContentDispositionFormData("attachment", det.getDocumentoNome());
//
//        // Retorna a resposta com o arquivo
//        return ResponseEntity.ok()
//            .headers(headers)
//            .body(fileResource);
//    }

}
