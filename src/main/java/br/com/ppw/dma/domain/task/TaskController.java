package br.com.ppw.dma.domain.task;

import br.com.ppw.dma.domain.ambiente.AmbienteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RestController
@RequestMapping("task")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class TaskController {

    TaskService taskService;
    AmbienteService ambienteService;


    @Autowired
    public TaskController(TaskService taskService, AmbienteService ambienteService) {
        this.taskService = taskService;
        this.ambienteService = ambienteService;
    }

    @PostMapping("ambiente/{ambienteId}")
    public ResponseEntity<TaskPushResponseDTO> addNewTask(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable(name = "ambienteId") Long ambienteId,
        @Valid @RequestBody TaskPayload taskPayload)
    throws JsonProcessingException {
        var ambiente = ambienteService.findById(ambienteId);
        var usuario = jwt.getSubject();
        var dto = taskService.pushTaskToQueue(ambiente, usuario, taskPayload);
        return ResponseEntity.ok(dto);
    }

    //TODO: criar um DTO para receber essa requisição
    @GetMapping("summary/ambiente/{ambienteId}")
    public ResponseEntity<Page<TaskInfoDTO>> getAllSummarized(
        @PathVariable(name = "ambienteId") Long ambienteId,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens,
        @RequestParam(name = "ticket") Optional<String> ticket,
        @RequestParam(name = "nomePipeline") Optional<String> nomePipeline,
        @RequestParam(name = "autor") Optional<String> autor)
    {
        var pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        var ambiente = ambienteService.findById(ambienteId);
        var exemplo = RemoteTask.builder()
            .ticket(ticket.orElse(null))
            .pipelineNome(nomePipeline.orElse(null))
            .usuario(autor.orElse(null))
            .ambienteId(ambiente.getId())
            .build();
        Page<TaskInfoDTO> relatorios = taskService.findAllByExample(exemplo, pageConfig)
            .map(TaskInfoDTO::converterEntidade);
        return ResponseEntity.ok(relatorios);
    }

}
