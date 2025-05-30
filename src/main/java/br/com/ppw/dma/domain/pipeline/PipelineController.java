package br.com.ppw.dma.domain.pipeline;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.cliente.ClienteService;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.job.JobInfoDTO;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.massa.MassaColunaDTO;
import br.com.ppw.dma.domain.massa.MassaTabela;
import br.com.ppw.dma.domain.massa.MassaTabelaService;
import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.master.QueryFilter;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.pipeline.execution.PipelineExecDTO;
import br.com.ppw.dma.domain.pipeline.execution.PipelineJobInputDTO;
import br.com.ppw.dma.domain.pipeline.execution.PipelineQueryInputDTO;
import br.com.ppw.dma.domain.task.RemoteTask;
import br.com.ppw.dma.domain.task.*;
import br.com.ppw.dma.domain.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.domain.storage.FileSystemService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.util.FormatDate;
import br.com.ppw.dma.util.FormatString;
import br.com.ppware.api.GeradorDeMassa;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ValidationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("pipeline")
public class PipelineController extends MasterController<Long, Pipeline, PipelineController> {

    private PipelineService pipelineService;
    private ClienteService clienteService;
    private AmbienteService ambienteService;
    private JobService jobService;
    private TaskService taskService;
    private MassaTabelaService massaService;
    private FileSystemService fileSystemService;


    @Autowired
    public PipelineController(
        PipelineService pipelineService,
        ClienteService clienteService,
        AmbienteService ambienteService,
        JobService jobService,
        TaskService taskService,
        MassaTabelaService massaService,
        FileSystemService fileSystemService) {
        //--------------------------------------------
        super(pipelineService);
        this.pipelineService = pipelineService;
        this.clienteService = clienteService;
        this.ambienteService = ambienteService;
        this.jobService = jobService;
        this.taskService = taskService;
        this.massaService = massaService;
        this.fileSystemService = fileSystemService;
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<?> getAll(@PathVariable(name = "clienteId") Long clienteId) {
        final List<PipelineInfoDTO> dtos = pipelineService.findAllByCliente(clienteId)
            .stream()
            .map(PipelineInfoDTO::new)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<?> parseOne(Pipeline entity) {
        final PipelineInfoDTO dto = new PipelineInfoDTO(entity);
        return ResponseEntity.ok(dto);
    }

    /**
     * Método herdado do {@link MasterController} para conversão de Entidade para DTO
     * @param pipelines {@link Page} de {@link Pipeline}s a converter
     * @return {@link ResponseEntity} contendo uma {@link Page} de {@link PipelineInfoDTO}s
     */
    @Override
    public ResponseEntity<Page<PipelineInfoDTO>> parseAll(Page<Pipeline> pipelines) {
        final Page<PipelineInfoDTO> responsePage = pipelines.map(PipelineInfoDTO::new);
        return ResponseEntity.ok(responsePage);
    }

    /**
     * Prepara e valida uma {@link Pipeline} solciitada para ser corretamente executada no
     * {@link Ambiente} remoto.
     * <ol>
     * <li>Identifica o {@link Ambiente} para obter os acessos remotos</li>
     * <li>Valida se os Jobs declarados ({@link PipelineJobInputDTO}) estão 100% alinhados com
     * os {@link Job}s da {@link Pipeline}</li>
     * <li>Valida se o nome das Massas declaradas existe no banco</li>
     * <li>Agrupa cada Job declarado com seu respectivo {@link Pipeline} {@link Job} para gerar a
     * lista de {@link TaskPayloadJob}
     * </li>
     * <li>Coleta e vincula metadados no banco remoto para todas as {@link MassaTabela} solicitadas</li>
     * <li>Gera valores aleatórios para todas as {@link MassaTabela} solicitadas</li>
     * <li>Identifica as Variáveis da Pipeline que solicitam {@link MassaTabela} para substituir
     * pelos valores gerados</li>
     * <li>Identifica se Variáveis da Pipeline para {@link MassaTabela} ficou pendente</li>
     * <li>Coleta o estado final de todas as queries (Jobs e Massas) para testá-las no banco remoto</li>
     * <li>Valida se é possível executar com base no status da fila ({@link RemoteTask})</li>
     * </ol>
     * @param execDto {@link PipelineExecDTO} contendo as informações necessárias para execução.
     * @return {@link ResponseEntity} com o {@link TaskPushResponseDTO} contendo status da solicitação.
     */
    @Transactional
    @PostMapping(value = "run") //"run/pipelineId/{pipelineId}/ambienteId/{ambienteId}")
    public ResponseEntity<TaskPushResponseDTO> validadeToEnqueue(
        @RequestBody PipelineExecDTO execDto)
    throws JsonProcessingException, DuplicatedRecordException {
        log.info("Obtendo e validando Ambiente e Pipeline.");
        var ambiente = ambienteService.findById(execDto.getAmbienteId());
        var pipeline = pipelineService.findById(execDto.getPipelineId());
        var cliente = ambiente.getCliente();
        var jobsInfo = pipeline.getJobs()
            .stream()
            .map(JobInfoDTO::converterJob)
            .collect(Collectors.toList());
        var inputJobs = execDto.getJobs();
        var inputQueries = inputJobs.stream()
            .map(PipelineJobInputDTO::getQueries)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
        var massasNome = execDto.getMassas();
        var inconformidades = new ArrayList<String>();
        var usuario = execDto.getUser();

        // ------------- VALIDANDO INPUT JOBS -------------
        log.info("Comparando Jobs declarados x Jobs na Pipeline.");
        var jobsIds = jobsInfo.stream()
            .map(JobInfoDTO::getId)
            .collect(Collectors.toSet());
        var inputsJobsIds = inputJobs.stream()
            .map(PipelineJobInputDTO::getId)
            .collect(Collectors.toSet());

        var jobsIdsPendentes = new LinkedList<Long>(jobsIds);
        jobsIdsPendentes.removeAll(inputsJobsIds);
        var inputsJobsIdsPendentes = new LinkedList<Long>(inputsJobsIds);
        inputsJobsIdsPendentes.removeAll(jobsIds);

        if(!jobsIdsPendentes.isEmpty()) {
            var pendentes = "Jobs da Pipeline não declarados: " + jobsIdsPendentes
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            log.warn(pendentes);
            inconformidades.add(pendentes);
        }
        if(!inputsJobsIdsPendentes.isEmpty()) {
            var pendentes = "Jobs declarados não encontrados na Pipeline: " + inputsJobsIdsPendentes
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            log.warn(pendentes);
            inconformidades.add(pendentes);
        }
        // ------------- VALIDANDO INPUT MASSAS -------------
        log.info("Obtendo as Massas declaradas para validar pendências.");
        var massasPendentes = new ArrayList<String>();
        var massasBanco = massaService.findByClienteIdAndNomes(cliente.getId(), massasNome)
            .stream()
            .map(MassaTabela::toDto)
            .toList();

        massasNome.stream()
            .filter(nome ->  massasBanco.stream().noneMatch(massa -> massa.getNome().equals(nome)))
            .forEach(massasPendentes::add);

        if(!massasPendentes.isEmpty()) {
            var pendentes = String.format("Massas não identificadas para o Cliente '%s': %s",
                cliente.getNome(),
                String.join(", ", massasPendentes));
            log.warn(pendentes);
            inconformidades.add(pendentes);
        }
//        log.info("Validando conflitos entre as variáveis dos Jobs e as configurações da Pipeline.");
//        try {
//            execDto.validar();
//        }
//        catch(Exception e) {
//            inconformidades.add(e.getMessage());
//        }

        if(!inconformidades.isEmpty())
            throw new ValidationException(String.join("\n", inconformidades));

        // ------------- PREPARANDO MASSA -------------
        //TODO: não parece precisar consultar os metadados das queries dos Jobs... (?!)
        var tables = new HashSet<String>();
        var columns = new HashSet<String>();
        inputQueries.stream()
            .map(PipelineQueryInputDTO::getSql)
            .map(SqlSintaxe::analyse)
            .forEach(extraction -> {
                tables.addAll(extraction.tables());
                extraction.filters()
                    .stream()
                    .map(QueryFilter::column)
                    .forEach(columns::add);
            });
        massasBanco.forEach(massa -> {
            tables.add(massa.getNome());
            massa.getColunas()
                .stream()
                .map(MassaColunaDTO::getNome)
                .forEach(columns::add);
        });
        var metadadosBanco = ambienteService.getMetadatasFromTables(tables, columns, ambiente);

        log.info("Metadados obtidos no banco:");
        metadadosBanco.stream()
            .peek(tabelaDb -> log.info(tabelaDb.toString()))
            .forEach(
                tableDb -> massasBanco.stream().forEach(
                    massa -> massa.atualizar(tableDb)
            ));
        log.info("Estado das Massas atualizadas:");
        massasBanco.forEach(massa -> log.info(massa.toString()));

        //TODO: o formato de data deveria estar em Ambiente.bancoDataFormato ou Global.bancoDataFormato.
        log.info("Gerando Massas com dados atualizados.");
        var massasPreparadas = GeradorDeMassa.mapearMassa(FormatDate.BRASIL_STYLE, massasBanco);

        log.info("Estado final das Massas geradas:");
        massasPreparadas.forEach(massa -> log.info(massa.toString()));

        // ------------- APLICANDO VARIÁVEIS -------------
        //TODO: Mover parte desse método para novo endpoint, focado na validação das solicitações no DTO.
        log.info("Aplicando valores das Massas nas variáveis globais da Pipeline.");
        execDto.getConfiguracoes()
            .entrySet()
            .stream()
            .filter(variavel -> variavel.getValue().matches("^\\$[^.]*\\..*"))
            .forEach(variavel -> {
                log.info(" - Identificada Variável Global solicitando Massa: {}", variavel);
                var campoArray = variavel.getValue().split("\\.");
                var tabelaNome = campoArray[0].substring(1);
                var colunaNome = campoArray[1];
                massasPreparadas.stream()
                    .filter(massa -> massa.getTabela().equalsIgnoreCase(tabelaNome))
                    .findFirst()
                    .flatMap(massa -> massa.getColunasSqlSintaxe(colunaNome))
                    .ifPresent(valor -> {
                        log.info(" - Novo valor: {}", valor);
                        variavel.setValue(valor);
                    });
            });
        log.info("Estado final das Variáveis da Pipeline:");
        var variaveisPendentes = execDto.getConfiguracoes()
            .entrySet()
            .stream()
            .peek(variavel -> log.info(variavel.toString()))
            .map(Map.Entry::getValue)
            .filter(FormatString::possuiVariaveis)
            .collect(Collectors.joining(", "));
        if(!variaveisPendentes.isBlank())
            throw new ValidationException("Variáveis pendentes: " + variaveisPendentes);

        // ------------- PREPARANDO JOBS -------------
        log.info("Aplicando as Variáveis da Pipeline nos Jobs solicitados.");
        inputJobs.forEach(job -> job.setVariaveis(execDto.getConfiguracoes()));
        var jobsQueue = TaskPayloadJob.matchAndMerge(jobsInfo, inputJobs);

        // ------------- TRATAMENTO FINAL DE QUERIES -------------
        log.info("Estado final das queries dos Jobs:");
        var queriesToTest = jobsQueue.stream()
            .map(TaskPayloadJob::getQueriesExec)
            .flatMap(List::stream)
            .map(TaskPayloadQuery::getQuery)
            .peek(log::info)
            .collect(Collectors.toSet());

        log.info("Estado final das queries das Massas.");
        var massaInsert = new ArrayList<TaskPayloadQuery>();
        var massaDelete = new ArrayList<TaskPayloadQuery>();
        massasPreparadas.forEach(massa -> {
            var nome = "Insert " + massa.getTabela();
            var descricao = "Gerando massa " + massa.getTabela();
            var sql = FormatString.substituirVariaveis(
                massa.gerarQueryInsert(),
                massa.getColunasSqlSintaxe(),
                ":(\\w+)");
            queriesToTest.add(sql);
            log.info(sql);
            massaInsert.add(TaskPayloadQuery.DML(nome, descricao, sql));

            nome = "Delete " + massa.getTabela();
            descricao = "Gerando massa " + massa.getTabela();
            sql = FormatString.substituirVariaveis(
                massa.gerarQueryDelete(),
                massa.getColunasSqlSintaxe(),
                ":(\\w+)");
            queriesToTest.add(sql);
            log.info(sql);
            massaDelete.add(TaskPayloadQuery.DML(nome, descricao, sql));
        });
        log.info("Validando estado final das queries nos Jobs e Massas.");
        ambienteService.validadeQuerySQL(queriesToTest, ambiente);

        // ------------- GERANDO SOLICITAÇÃO NA FILA DE EXECUÇÃO -------------
        var solicitacao = TaskPayload.builder()
            .pipelineNome(pipeline.getNome())
            .pipelineDescricao(pipeline.getDescricao())
            .queriesPrePipeline(massaInsert)
            .queriesPosPipeline(massaDelete)
            .jobs(jobsQueue)
            .build();
        var queueResponse = taskService.pushTaskToQueue(ambiente, usuario, solicitacao);
        return ResponseEntity.ok(queueResponse);
    }

    @PostMapping(value = "new")
    public ResponseEntity<?> createNew(@RequestBody PipelineInfoDTO dto)
    throws DuplicatedRecordException {
        pipelineService.checkDuplicated(dto.getNome(), dto.getClienteId());
        val cliente = clienteService.findById(dto.getClienteId());
        val jobs = jobService.findByClienteAndNome(cliente, dto.getJobs());
        val pipeline = Pipeline.parseInfoDto(dto, jobs, cliente);
        pipelineService.persist(pipeline);

        dto.setId(pipeline.getId());
        dto.setJobs(
            jobs.stream().map(Job::getNome).toList()
        );
        log.info("Resposta ao cliente:");
        log.info(dto.toString());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "update")
    public ResponseEntity<String> update(@RequestBody PipelineInfoDTO dto) {
        val pipeline = pipelineService.getUniqueOne(dto.getNome(), dto.getClienteId())
            .orElseThrow(() -> {
                var mensagem = String.format("Pipeline %s não encontrada no banco para Cliente ID %d",
                    dto.getNome(),
                    dto.getClienteId());
                return new NoSuchElementException(mensagem);
            });
        val cliente = pipeline.getCliente();
        log.info("Comparando Pipelines.");
        log.info("Pipelines Usuário: {}", dto);
        log.info("Pipelines Banco: {}", pipeline);
        val precisaAtualizarDescricao = pipeline.precisaAtualizarDescricao(dto.getDescricao());
        val precisaAtualizarJobs = pipeline.precisaAtualizarJobs(dto.getJobs());

        if(!precisaAtualizarDescricao && !precisaAtualizarJobs) {
            log.info("A Pipeline consta já conforme o solicitado. Sem atualizações.");
            return ResponseEntity.ok("Pipeline não precisa ser atualizada");
        }
        log.info("A Pipeline precisa ser atualizada.");
        if(precisaAtualizarDescricao) {
            pipeline.setDescricao(dto.getDescricao());
        }
        if(precisaAtualizarJobs) {
            var jobs = jobService.findByClienteAndNome(cliente, dto.getJobs());

            log.info("Ordenando os Jobs da Pipeline conforme solicitado:");
            log.info(String.join(", ", dto.getJobs()));
            pipeline.getJobs().clear();
            for(var jobNome : dto.getJobs()) {
                jobs.stream()
                    .filter(job -> job.getNome().equals(jobNome))
                    .findFirst()
                    .ifPresent(job -> {
                        log.info("Adicionando Job {} [{}].", job.getNome(), job.getId());
                        pipeline.getJobs().add(job);
                    });
            }
            log.info("Resultado da ordenação dos Jobs na Pipeline:");
            log.info(pipeline.toString());
        }
        pipelineService.persist(pipeline);
        return ResponseEntity.ok("Pipeline atualizada");
    }

//    public Pipeline createNewPipeline(@NonNull PipelineExecDTO execDTO) {
//        log.info("Criando nova Pipeline '{}' para Cliente ID {}.",
//            execDTO.getPipelineId().getNome(), execDTO.getClienteId());
//        val cliente = clienteService.findById(execDTO.getClienteId());
//
//        log.info("Obtendo todos os Jobs listados no DTO.");
//        val jobIds = execDTO.getJobs()
//            .stream()
//            .map(PipelineJobInputDTO::getId)
//            .toList();
//        final List<Job> jobs = jobService.findAllById(jobIds);
//        val jobsNome = jobs.stream()
//            .map(Job::getNome)
//            .collect(Collectors.joining(", "));
//        log.info("Total de {} Jobs encontrados:", jobs.size());
//        log.info(" - {}", jobsNome);
//
//        val pipeline = Pipeline.parseInfoDto(execDTO.getPipelineId(), jobs, cliente);
//        return pipelineService.persist(pipeline);
//    }


    @DeleteMapping(value = "clientId/{clientId}/pipeline/{name}")
    public ResponseEntity<String> delete(
        @PathVariable(name = "clientId") Long clientId,
        @PathVariable(name = "name") String nome)
    {
        //TODO: nome.matches("[a-zA-Z0-9_-]+") ...?
        var pipeline = pipelineService.getUniqueOne(nome, clientId);
        if (pipeline.isPresent()) {
            pipeline.get().setOcultar(true);
            pipelineService.persist(pipeline.get());
        }
        else {
            return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Pipeline '" + Encode.forHtml(nome) + "' não encontrada.");
        }
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body("Pipeline '" + Encode.forHtml(nome) + "' deletada com sucesso.");
    }

}
