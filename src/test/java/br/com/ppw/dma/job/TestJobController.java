package br.com.ppw.dma.job;

import br.com.ppw.dma.DetMakerApplication;
import br.com.ppw.dma.domain.cliente.ClienteRepository;
import br.com.ppw.dma.domain.job.*;
import br.com.ppware.GeradorAleatorio;
import br.com.ppware.LocalAleatorio;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DetMakerApplication.class)
public class TestJobController {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JobController jobController;
    @Autowired JobRepository jobRepository;
    @Autowired ClienteRepository clienteRepository;

    Random random = new Random();
    List<JobInfoDTO> dtos = new ArrayList<>();
    Long clientId;

    static final long DEFAULT_NEW_JOB_ID = -1L;


    @BeforeEach
    public void generatingJobsDto() {
        log.info("-----------------------------------------------------");
        log.info("Preparando massa de teste.");

        var clients = clienteRepository.findAll();
        if(clients.isEmpty())
            throw new RuntimeException("O teste precisa precisa de ao menos 1 cliente na base.");
        clientId = clients.get(0).getId();
        log.info("Cliente ID sendo usado no teste: [{}]", clientId);

        var totalDtosToCreate = random.nextInt(9) + 3;
        var allJobsName = new ArrayList<String>();
        var allParametersName = new ArrayList<String>();
        var allCargasName = new ArrayList<String>();
        var allLogsName = new ArrayList<String>();
        var allRemessasName = new ArrayList<String>();

        log.info("Gerando {} DTOs de Job.", totalDtosToCreate);
        var jobsDto = Stream.generate(() -> true)
            .limit(totalDtosToCreate)
            .map(vazio -> {
                var paramsDto = Stream.generate(() -> true)
                    .limit(random.nextInt(3) + 1)
                    .map(nada -> randomNewJobParameterDto())
                    .collect(Collectors.toList());
                var cargasDto = Stream.generate(() -> true)
                    .limit(random.nextInt(2) + 1)
                    .map(nada -> randomNewJobResourceDto(JobResourceType.CARGA))
                    .collect(Collectors.toList());
                var logsDto = Stream.generate(() -> true)
                    .limit(random.nextInt(3) + 1)
                    .map(nada -> randomNewJobResourceDto(JobResourceType.LOG))
                    .collect(Collectors.toList());
                var remessasDto = Stream.generate(() -> true)
                    .limit(random.nextInt(2) + 1)
                    .map(nada -> randomNewJobResourceDto(JobResourceType.REMESSA))
                    .collect(Collectors.toList());

                var dto = new JobInfoDTO();
                dto.setId(DEFAULT_NEW_JOB_ID);
                dto.setNome(UUID.randomUUID().toString());
                dto.setDescricao(LocalAleatorio.endereco());
                dto.setCaminhoExec("/app/teste/algo/" + GeradorAleatorio.cpf());
                dto.setParametros(paramsDto);
                dto.setMascaraEntrada(cargasDto);
                dto.setMascaraLog(logsDto);
                dto.setMascaraSaida(remessasDto);
                log.info(dto.toString());

                allJobsName.add(dto.getNome());
                allParametersName.addAll(paramsDto.stream().map(JobParamDTO::getNome).toList());
                allCargasName.addAll(cargasDto.stream().map(JobResourceDTO::getEndereco).toList());
                allLogsName.addAll(logsDto.stream().map(JobResourceDTO::getEndereco).toList());
                allRemessasName.addAll(remessasDto.stream().map(JobResourceDTO::getEndereco).toList());

                return dto;
            })
            .collect(Collectors.toList());
        dtos.addAll(jobsDto);

        log.info("Nome dos DTOs de Job criados para o teste:");
        log.info("{}", allJobsName);
        log.info("Nome dos DTOs dos parâmetros criados para o teste:");
        log.info("{}", allParametersName);
        log.info("Nome dos DTOs das cargas criados para o teste:");
        log.info("{}", allCargasName);
        log.info("Nome dos DTOs dos logs criados para o teste:");
        log.info("{}", allLogsName);
        log.info("Nome dos DTOs das remessas criados para o teste:");
        log.info("{}", allRemessasName);

    }

    private static JobResourceDTO randomNewJobResourceDto(JobResourceType type) {
        var resource = new JobResourceDTO();
        resource.setTipo(type.name());
        resource.setEndereco(UUID.randomUUID().toString());
        resource.setDescricao(LocalAleatorio.endereco());
        resource.setProtocolo(JobResourceProtocolType.FTP.name());
        return resource;
    }

    private static JobParamDTO randomNewJobParameterDto() {
        var param = new JobParamDTO();
        param.setTipo(JobParameterType.STRING.name());
        param.setNome(UUID.randomUUID().toString().substring(0, 29));
        param.setDescricao(LocalAleatorio.endereco());
        param.setOpcional(false);
        return param;
    }

    @AfterEach
    public void removingAllJobsDtoFromBase() {
        log.info("-----------------------------------------------------");
        log.info("Deletando Jobs gerados pelos DTOs.");
        var cliente = clienteRepository.findById(clientId).orElseThrow();
        var dtosToDelete = dtos.stream()
            .map(JobInfoDTO::getNome)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        log.info(dtosToDelete.toString());
        var jobsToDelete = jobRepository.findByClienteAndNomeIn(cliente, dtosToDelete);
        jobRepository.deleteAll(jobsToDelete);
        log.info("Jobs deletados com sucesso.");
    }

    @Test
    public void testCreateAndUpdteJobs() throws Exception {
        log.info("Testando criar e atualizar Jobs usando {} DTOs.", dtos.size());
        log.info("-----------------------------------------------------");
        log.info("Etapa 1: Criando Jobs.");
        var dtosCreated = requestNewJobs(clientId, dtos);

        log.info("-----------------------------------------------------");
        log.info("Etapa 2: Validando DTOs Retornados Após Criação.");
        assertNotNull(dtosCreated, () -> "Criação de Jobs com retorno nulo, mesmo com status de sucesso.");
        dtosCreated.forEach(this::validadeJobDtoReturned);

        log.info("-----------------------------------------------------");
        log.info("Etapa 3: Atualizando Alguns Dados de Um Job.");
        var dtoToUpdate = dtos.stream().findAny().orElseThrow();
        log.info("Job a ser atualizado: {} [ID {}]", dtoToUpdate.getNome(), dtoToUpdate.getId());
        dtoToUpdate.setNome(UUID.randomUUID().toString());
        dtoToUpdate.setDescricao("Teste");
        dtoToUpdate.setCaminhoExec("Teste");
        log.info(dtoToUpdate.toString());
        var dtoUpdated = requestUpdateJob(clientId, dtoToUpdate);

        log.info("-----------------------------------------------------");
        log.info("Etapa 4: Validando DTO Retornado Após Atualização.");
        assertNotNull(dtoUpdated, () -> "Atualização de Job com retorno nulo, mesmo com status de sucesso.");
        validadeJobDtoReturned(dtoToUpdate, dtoUpdated);

        log.info("-----------------------------------------------------");
        log.info("Etapa 5: Atualizando Apenas Parâmetros.");
        var dtoToUpdateParameters = dtos.stream().findAny().orElseThrow();
        log.info("Job a atualizar parâmetros: {} [ID {}]", dtoToUpdateParameters.getNome(), dtoToUpdateParameters.getId());
        dtoToUpdateParameters.setParametros(Stream.generate(() -> true)
            .limit(random.nextInt(4))
            .map(nada -> randomNewJobParameterDto())
            .collect(Collectors.toList()));
        log.info(dtoToUpdateParameters.toString());
        var dtoParametersUpdated = requestUpdateJob(clientId, dtoToUpdateParameters);

        log.info("-----------------------------------------------------");
        log.info("Etapa 6: Validando DTO Retornado Após Atualização dos Parâmetros.");
        assertNotNull(dtoParametersUpdated, () -> "Atualização dos parâmetros do Job com retorno nulo, mesmo com status de sucesso.");
        validadeJobDtoReturned(dtoToUpdateParameters, dtoParametersUpdated);

        log.info("-----------------------------------------------------");
        log.info("Etapa 7: Atualizando Apenas Cargas.");
        var dtoToUpdateCargas = dtos.stream().findAny().orElseThrow();
        log.info("Job a atualizar cargas: {} [ID {}]", dtoToUpdateCargas.getNome(), dtoToUpdateCargas.getId());
        dtoToUpdateCargas.setMascaraEntrada(Stream.generate(() -> true)
            .limit(random.nextInt(4))
            .map(nada -> randomNewJobResourceDto(JobResourceType.CARGA))
            .collect(Collectors.toList()));
        log.info(dtoToUpdateCargas.toString());
        var dtoCargasUpdated = requestUpdateJob(clientId, dtoToUpdateCargas);

        log.info("-----------------------------------------------------");
        log.info("Etapa 8: Validando DTO Retornado Após Atualização dos Parâmetros.");
        assertNotNull(dtoCargasUpdated, () -> "Atualização das cargas do Job com retorno nulo, mesmo com status de sucesso.");
        validadeJobDtoReturned(dtoToUpdateCargas, dtoCargasUpdated);
    }

    private List<JobInfoDTO> requestNewJobs(Long clientId, Collection<JobInfoDTO> dtos) throws Exception {
        var listJobType = new TypeReference<List<JobInfoDTO>>() {};
        var requestJson = objectMapper.writeValueAsString(dtos);
        var responseBody = mockMvc.perform(
                MockMvcRequestBuilders.post("/job/cliente/%d/all".formatted(clientId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readValue(responseBody, listJobType);
    }

    private JobInfoDTO requestUpdateJob(Long clientId, JobInfoDTO jobDto) throws Exception {
        var requestJson = objectMapper.writeValueAsString(jobDto);
        var responseBody = mockMvc.perform(
                MockMvcRequestBuilders.put("/job/cliente/%d".formatted(clientId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readValue(responseBody, JobInfoDTO.class);
    }

    private void validadeJobDtoReturned(JobInfoDTO jobRetornado) {
        var alvo = "Job retornado '%s': ".formatted(jobRetornado.getNome());
        var jobEnviado = dtos.stream()
            .filter(dto -> dto.getNome().equals(jobRetornado.getNome()))
            .findFirst()
            .orElse(null);
        assertNotNull(jobEnviado, () -> alvo + "não consta na lista dos DTOs enviados.");

        validadeJobDtoReturned(jobEnviado, jobRetornado);
    }

    private static void validadeJobDtoReturned(JobInfoDTO jobEnviado, JobInfoDTO jobRetornado) {
        var alvo = "Job retornado '%s': ".formatted(jobRetornado.getNome());

        //Dados internos do Job
        assertNotNull(jobRetornado.getId(), () -> alvo + "retornado sem ID preenchido.");
        assertNotEquals(
            DEFAULT_NEW_JOB_ID,
            jobRetornado.getId(),
            () -> alvo + "retornado com ID igual ao que foi enviado."
        );
        assertEquals(
            jobEnviado.getNome(),
            jobRetornado.getNome(),
            () -> alvo + "com nome diferente do DTO enviado."
        );
        assertEquals(
            jobEnviado.getDescricao(),
            jobRetornado.getDescricao(),
            () -> alvo + "com descrição diferente do DTO enviado."
        );
        assertEquals(
            jobEnviado.getCaminhoExec(),
            jobRetornado.getCaminhoExec(),
            () -> alvo + "com caminho de execução diferente do DTO enviado."
        );
        //Validando parâmetros
        assertEquals(
            jobEnviado.getParametros().size(),
            jobRetornado.getParametros().size(),
            () -> alvo + "quantidade de parâmetros retornados difere dos enviados"
        );
        jobRetornado.getParametros().forEach(paramRetornado -> assertTrue(
            jobEnviado.getParametros().contains(paramRetornado),
            () -> "%s parâmetro '%s' não consta nos enviados".formatted(alvo, paramRetornado.getNome())
        ));
        //Validando cargas
        assertEquals(
            jobEnviado.getMascaraEntrada().size(),
            jobRetornado.getMascaraEntrada().size(),
            () -> alvo + "quantidade de cargas retornadas difere das enviadas"
        );
        jobRetornado.getMascaraEntrada().forEach(cargaRetornada -> assertTrue(
            jobEnviado.getMascaraEntrada().contains(cargaRetornada),
            () -> "%s carga '%s' não consta nas enviadas".formatted(alvo, cargaRetornada.getEndereco())
        ));
        //Validando logs
        assertEquals(
            jobEnviado.getMascaraLog().size(),
            jobRetornado.getMascaraLog().size(),
            () -> alvo + "quantidade de logs retornados difere dos enviados"
        );
        jobRetornado.getMascaraLog().forEach(logRetornado -> assertTrue(
            jobEnviado.getMascaraLog().contains(logRetornado),
            () -> "%s log '%s' não consta nos enviados".formatted(alvo, logRetornado.getEndereco())
        ));
        //Validando remessas
        assertEquals(
            jobEnviado.getMascaraSaida().size(),
            jobRetornado.getMascaraSaida().size(),
            () -> alvo + "quantidade de remessas retornadas difere das enviadas"
        );
        jobRetornado.getMascaraSaida().forEach(remessaRetornada -> assertTrue(
            jobEnviado.getMascaraSaida().contains(remessaRetornada),
            () -> "%s remessa '%s' não consta nas enviadas".formatted(alvo, remessaRetornada.getEndereco())
        ));
    }

}
