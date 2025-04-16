package br.com.ppw.dma;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.ambiente.AmbienteRepository;
import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.job.JobRepository;
import br.com.ppw.dma.domain.task.TaskPayload;
import br.com.ppw.dma.domain.task.TaskPayloadJob;
import br.com.ppw.dma.domain.task.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.domain.task.TaskStatus.AGUARDANDO;
import static br.com.ppw.dma.domain.task.TaskStatus.EXECUTANDO;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(classes = DetMakerApplication.class)
class TaskControllerTest {

    Random random = new Random();
    LocalDate atualIfxdate = LocalDate.of(2025, Month.FEBRUARY, 25);
    DateTimeFormatter ifxdateFormatter = DateTimeFormatter.ofPattern("YYYYMMdd");
    List<Ambiente> ambientes = new ArrayList<>();
    Map<Ambiente, List<TaskPayload>> allTasks = new ConcurrentHashMap<>();

    @Autowired AmbienteRepository ambienteRepository;
    @Autowired JobRepository jobRepository;
    @Autowired TaskService taskService;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;


    @BeforeEach
    public void generateTasks() {
        log.info("----------------------------------------------------------------------------");
        log.info("Iniciando preparo das massas para os testes.");
        ambientes.addAll(ambienteRepository.findAll());
        log.info("Total e ambientes na base: [{}]", ambientes.size());
        if(ambientes.isEmpty())
            throw new RuntimeException("Nenhum ambiente disponível para gerar os testes.");

        var jobsPerCliente = ambientes.stream().map(Ambiente::getCliente)
            .map(Cliente::getJobs)
            .flatMap(Collection::stream)
            .filter(job -> job.getNome().contains("ifxdate_inicia.ksh"))
            .collect(Collectors.groupingBy(
                Job::getCliente
            ));

        var totalTasksPerAmbiente = random.nextInt(10) + 1;
        ambientes.stream()
            .map(ambiente -> Map.entry(
                ambiente,
                generateTaskPayload(totalTasksPerAmbiente)
            ))
            .forEach(entry -> allTasks.put(entry.getKey(), entry.getValue()));

        log.info("Todas as Tasks geradas para cada Ambiente:");
        allTasks.forEach((ambiente, tasks) -> tasks.forEach(
            task -> log.info("[Ambiente ID {}] - {}", ambiente.getId(), task)
        ));
        log.info("----------------------------------------------------------------------------");
    }

    private List<TaskPayload> generateTaskPayload(int amount) {
        var count = new AtomicInteger(0);
        return Stream.generate(count::getAndIncrement)
            .limit(amount)
            .map(index -> TaskPayload.builder()
                .pipelineNome("Teste " + index)
                .pipelineDescricao(TaskControllerTest.class.getSimpleName())
                .jobs(generateTasksPayloadJob(random.nextInt(5) + 1))
                .build())
            .collect(Collectors.toList());
    }

    public List<TaskPayloadJob> generateTasksPayloadJob(int amount) {
        var count = new AtomicInteger(0);
        return Stream.generate(count::getAndIncrement)
            .limit(amount)
            .map(index -> TaskPayloadJob.builder()
                .nome("cy3_shell_ifxdate_inicia.ksh")
                .descricao("Altera a data do Ciclo Diário. Avaliar start de time = 08:00hs + termina da diaria anterior. Deve ser disparado logo após a finalização do ciclo anterior (pela manhã)")
                .ordem(index)
                .comandoExec("ksh /app/rcvry/shells/cy3_shell_ifxdate_inicia.ksh " + atualIfxdate.plusDays(index).format(ifxdateFormatter))
                .comandoVersao("sha256sum /app/rcvry/shells/cy3_shell_ifxdate_inicia.ksh")
                .build())
            .collect(Collectors.toList());
    }


    @Test
    void testAddNewTasksInSequencialOrder() {
        log.info("Testando adicionar todas as Tasks em ordem sequencial.");

        allTasks.forEach((ambiente, tasks) -> {
            var requestTaskCount = new AtomicInteger(0);
            tasks.forEach(task -> {
                try {
                    var index = requestTaskCount.get();
                    var id = ambiente.getId();
                    var json = objectMapper.writeValueAsString(task);
                    var expectedStatus = (index == 0) ? EXECUTANDO : AGUARDANDO;

                    log.info("Enviando requisição Task[{}] para Ambiente ID {}.", index, id);
                    mockMvc.perform(post("/task/ambiente/{ambienteId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.ambienteId").value(id))
                        .andExpect(jsonPath("$.queueSize").value(index))
                        .andExpect(jsonPath("$.ticket").exists())
                        .andExpect(jsonPath("$.status").value(expectedStatus.name()));

                    requestTaskCount.incrementAndGet();
                    log.info("Requisição Task[{}] para Ambiente ID {} validada com sucesso.", index, id);
                }
                catch(Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
        log.info("Aguardando até que todas as Tasks tenham finalizadas.");
        taskService.waitForAllTasksCompletion();
    }

//    void testGetAllSummarizedSuccess(long ambienteId) throws Exception {
//        int page = 0;
//        int itens = 12;
//        log.info("Enviando requisição Task[{}] para Ambiente ID {}.", index, id);
//        mockMvc.perform(get("task/summary/ambiente/{ambienteId}", ambienteId)
//                .param("page", String.valueOf(page))
//                .param("itens", String.valueOf(itens))
//                .accept(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.content").isArray())
//            .andExpect(jsonPath("$.pageable.pageNumber").value(page))
//            .andExpect(jsonPath("$.pageable.pageSize").value(itens))
//            .andExpect(jsonPath("$.content[*].taskId").exists());
//    }

//    @Test
//    void testAddNewTaskInvalidInput() throws Exception {
//        // Arrange
//        Long ambienteId = 1L;
//        Task task = new Task();
//        // Deixando descrição vazia ou nula, assumindo validação @NotBlank
//        task.setDescription(""); // ou null
//
//        String taskJson = objectMapper.writeValueAsString(task);
//
//        // Act & Assert
//        mockMvc.perform(post("/ambiente/{ambienteId}", ambienteId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(taskJson))
//            .andExpect(status().isBadRequest())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.error").exists());
//    }

//    @Test
//    void testAddNewTaskAmbienteNotFound() throws Exception {
//        // Arrange
//        Long ambienteId = 999L; // ID inexistente
//        Task task = new Task();
//        task.setDescription("Run database migration");
//
//        String taskJson = objectMapper.writeValueAsString(task);
//
//        // Act & Assert
//        mockMvc.perform(post("/ambiente/{ambienteId}", ambienteId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(taskJson))
//            .andExpect(status().isNotFound())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//            .andExpect(jsonPath("$.error").value("Ambiente not found"));
//    }
}
