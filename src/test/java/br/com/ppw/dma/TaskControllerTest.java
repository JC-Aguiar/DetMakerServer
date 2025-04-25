package br.com.ppw.dma;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.ambiente.AmbienteRepository;
import br.com.ppw.dma.domain.cliente.ClienteRepository;
import br.com.ppw.dma.domain.task.TaskPayload;
import br.com.ppw.dma.domain.task.TaskPayloadJob;
import br.com.ppw.dma.domain.task.TaskPayloadQuery;
import br.com.ppw.dma.domain.task.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(classes = DetMakerApplication.class)
class TaskControllerTest {

    Random random = new Random();
    LocalDate atualIfxdate = LocalDate.of(2025, Month.FEBRUARY, 25);
    DateTimeFormatter ifxdateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    List<Ambiente> ambientes = new ArrayList<>();
    Map<Ambiente, List<TaskPayload>> allTasks = new ConcurrentHashMap<>();

    @Autowired AmbienteRepository ambienteRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired TaskService taskService;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;


    @BeforeEach
    public void generateTasks() {
        log.info("----------------------------------------------------------------------------");
        log.info("Iniciando preparo das massas para os testes.");
        var clientes = clienteRepository.findAllByNomeContaining("VIVO");
        ambientes.addAll(ambienteRepository.findAllByClienteIn(clientes));

        log.info("Total e Ambientes na base: [{}]", ambientes.size());
        if(ambientes.isEmpty())
            throw new RuntimeException("Nenhum ambiente disponível para gerar os testes.");

        var totalTasksPerAmbiente = random.nextInt(10) + 1;
        log.info("Total de Tasks a serem geradas por Ambiente: [{}]", totalTasksPerAmbiente);
        ambientes.stream()
            .map(ambiente -> generateTaskPayload(ambiente, totalTasksPerAmbiente))
            .forEach(entry -> allTasks.put(entry.getKey(), entry.getValue()));

        var totalJobs = allTasks.values().stream().mapToLong(List::size).sum();
        log.info("Total de Jobs criados: [{}]", totalJobs);

        log.info("Massas geradas:");
        allTasks.forEach((ambiente, tasks) -> tasks.forEach(
            task -> log.info("[Ambiente ID {}] - {}", ambiente.getId(), task)
        ));
        log.info("----------------------------------------------------------------------------");
    }

    private Map.Entry<Ambiente, List<TaskPayload>> generateTaskPayload(Ambiente ambiente, int amount) {
        var count = new AtomicInteger(0);
        var tasks = Stream.generate(count::getAndIncrement)
            .limit(amount)
            .map(index -> TaskPayload.builder()
                .pipelineNome("Ambiente ID %d - Teste %d".formatted(ambiente.getId(), index))
                .pipelineDescricao(TaskControllerTest.class.getSimpleName())
                .jobs(generateTasksPayloadJob(ambiente, random.nextInt(5) + 1))
                .build())
            .collect(Collectors.toList());
        return Map.entry(ambiente, tasks);
    }

    public List<TaskPayloadJob> generateTasksPayloadJob(Ambiente ambiente, int amount) {
        var count = new AtomicInteger(0);
        var ifxdateShellPath = switch(ambiente.getNome()) {
            case "VIVO1" -> "/cyberapp/rcvry/shells/vivo_ifxdate_inicia.ksh";
            case "VIVO3" -> "/app/rcvry/shells/cy3_shell_ifxdate_inicia.ksh";
            default -> "/app/rcvry/shells/*ifxdate_inicia.ksh";
        };
        return Stream.generate(count::getAndIncrement)
            .limit(amount)
            .map(index -> TaskPayloadJob.builder()
                .nome("Shell Início Ifxdate [%d]".formatted(index))
                .descricao("Altera a data do Ciclo Diário. Avaliar start de time = 08:00hs + termina da diaria anterior. Deve ser disparado logo após a finalização do ciclo anterior (pela manhã)")
                .ordem(index)
                .comandoExec("ksh %s %s".formatted(
                    ifxdateShellPath,
                    atualIfxdate.plusDays(index).format(ifxdateFormatter)))
                .comandoVersao("sha256sum %s | cut -d ' ' -f1".formatted(ifxdateShellPath))
                .queriesExec(generateTaskPayloadQuery(1))
                .build())
            .collect(Collectors.toList());
    }

    private List<TaskPayloadQuery> generateTaskPayloadQuery(int amount) {
        var count = new AtomicInteger(0);
        return Stream.generate(count::getAndIncrement)
            .limit(amount)
            .map(index -> TaskPayloadQuery.builder()
                .nome("Histórico de Eventos Semanal")
                .descricao("Coleta quais eventos Kafka foram processados pelo Cyber nos últimos 7 dias")
                .query("SELECT * FROM RCVRY.EVENTOS_WEB ew WHERE TRUNC(EVDTPROC) >= TRUNC(SYSDATE-7) ORDER BY EVID DESC")
                .build())
            .collect(Collectors.toList());
    }

    @Test
    void testAddNewTasksInSequencialOrder() {
        log.info("Testando adicionar todas as Tasks em ordem sequencial.");

        var allTickets = new ArrayList<String>();
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
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.ambienteId").value(id))
                        .andExpect(jsonPath("$.queueSize").value(index))
                        .andExpect(jsonPath("$.ticket").exists())
                        .andExpect(jsonPath("$.status").value(expectedStatus.name()))
                        .andDo(result -> {
                            var responseJson = result.getResponse().getContentAsString();
                            var ticket = JsonPath.<String>read(responseJson, "$.ticket");
                            allTickets.add(ticket);
                        });
                    requestTaskCount.incrementAndGet();
                    log.info("Requisição Task[{}] para Ambiente ID {} validada com sucesso.", index, id);
                }
                catch(Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
        log.info("Tickets obtidos:");
        allTickets.forEach(log::info);

        log.info("Coletando relatório de todos os Ambientes.");
        allTasks.forEach((ambiente, tasks) -> SummarizedTasksTestProps.builder()
            .controllerTes(this)
            .ambienteId(ambiente.getId())
            .expectedTotalElements(tasks.size())
            .build()
            .test()
        );
        log.info("Aguardando até que todas as Tasks tenham finalizadas.");
        taskService.waitForAllTasksCompletion();
    }

    @Builder
    record SummarizedTasksTestProps(
        @NonNull TaskControllerTest controllerTes,
        long ambienteId,
        int page,
        Integer itens,
        long expectedTotalElements,
        Integer expectedTotalPages)
    {
        public void test() {
            try {
                controllerTes.fetAllSummarizedTasksByAmbiente(this);
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void fetAllSummarizedTasksByAmbiente(@NonNull SummarizedTasksTestProps props) throws Exception {
        var ambienteId = props.ambienteId;
        var page = props.page;
        var itens = Optional.ofNullable(props.itens).orElse(12);
        var expectedTotalElements = props.expectedTotalElements;
        var expectedTotalPages = Optional.ofNullable(props.expectedTotalPages).orElse(1);

        log.info("Enviando requisição de relatório resumido das Tasks para Ambiente ID {}.", ambienteId);
        mockMvc.perform(get("/task/summary/ambiente/{ambienteId}", ambienteId)
                .param("page", String.valueOf(page))
                .param("itens", String.valueOf(itens))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.pageable.pageNumber").value(page))
            .andExpect(jsonPath("$.pageable.pageSize").value(itens))
            .andExpect(jsonPath("$.totalPages").value(expectedTotalPages))
            .andExpect(jsonPath("$.totalElements").value(expectedTotalElements))
            .andExpect(jsonPath("$.content[*].ticket").exists());
    }

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
//            .andExpect(jsonPath("$.fatalError").exists());
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
//            .andExpect(jsonPath("$.fatalError").value("Ambiente not found"));
//    }
}
