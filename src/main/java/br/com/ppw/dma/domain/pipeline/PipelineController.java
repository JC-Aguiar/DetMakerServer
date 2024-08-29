package br.com.ppw.dma.domain.pipeline;

import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.cliente.ClienteService;
import br.com.ppw.dma.domain.evidencia.EvidenciaService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.job.JobExecuteDTO;
import br.com.ppw.dma.domain.job.JobPreparation;
import br.com.ppw.dma.domain.job.JobService;
import br.com.ppw.dma.domain.massa.MassaColunaDTO;
import br.com.ppw.dma.domain.massa.MassaTabela;
import br.com.ppw.dma.domain.massa.MassaTabelaService;
import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.master.QueryFilter;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.domain.relatorio.RelatorioService;
import br.com.ppw.dma.domain.storage.FileSystemService;
import br.com.ppw.dma.util.FormatDate;
import br.com.ppw.dma.util.FormatString;
import br.com.ppware.api.GeradorDeMassa;
import jakarta.validation.ValidationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("pipeline")
public class PipelineController extends MasterController<Long, Pipeline, PipelineController> {

    @Autowired
    private ModelMapper mapper;

    private PipelineService pipelineService;

    private ClienteService clienteService;

    private AmbienteService ambienteService;

    private JobService jobService;

    private RelatorioService relatorioService;

    private EvidenciaService evidenciaService;

    private MassaTabelaService massaService;

    private FileSystemService fileSystemService;


    public PipelineController(
        @Autowired PipelineService pipelineService,
        @Autowired ClienteService clienteService,
        @Autowired AmbienteService ambienteService,
        @Autowired JobService jobService,
        @Autowired RelatorioService relatorioService,
        @Autowired EvidenciaService evidenciaService,
        @Autowired MassaTabelaService massaService,
        @Autowired FileSystemService fileSystemService) {
        //--------------------------------------------
        super(pipelineService);
        this.pipelineService = pipelineService;
        this.clienteService = clienteService;
        this.ambienteService = ambienteService;
        this.jobService = jobService;
        this.relatorioService = relatorioService;
        this.evidenciaService = evidenciaService;
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
     * Principal funcionalidade de toda a aplicação. Em ordem:<ol>
 *     <li>Identificação do Ambiente</li>
     * <li>Obtenção dos acessos ao Banco e FTP do Ambiente</li>
     * <li>Execução da pilha de Jobs</li>
     * <li>Coleta das Evidências de cada Job</li>
     * <li>Detalhamento do processo no Relatório</li>
     * </ol>
     * @param execDto {@link PipelineExecDTO} contendo as informações necessárias para execução.
     * @return {@link ResponseEntity} com o {@link RelatorioHistoricoDTO} do Relatório final.
     */
    @Transactional
    @PostMapping(value = "run") //"run/pipelineId/{pipelineId}/ambienteId/{ambienteId}")
    public ResponseEntity<RelatorioHistoricoDTO> validadeToEnqueu(@RequestBody PipelineExecDTO execDto) {
        //TODO: validar Ambiente?
        log.info("Obtendo e validando Ambiente e Pipeline.");
        var ambiente = ambienteService.findById(execDto.getAmbienteId());
        var pipeline = pipelineService.findById(execDto.getPipelineId());
        var cliente = ambiente.getCliente();
        var jobs = pipeline.getJobs();
        var inputJobs = execDto.getJobs();
        var inputQueries = inputJobs.parallelStream()
            .map(JobExecuteDTO::getQueries)
            .flatMap(Set::parallelStream)
            .collect(Collectors.toSet());
        var massasNome = execDto.getMassas();
        var inconformidades = new ArrayList<String>();

        // ------------- VALIDANDO INPUT JOBS -------------
        log.info("Comparando Jobs declarados x Jobs na Pipeline.");
        var jobsIds = jobs.parallelStream()
            .map(Job::getId)
            .collect(Collectors.toSet());
        var inputsJobsIds = inputJobs.parallelStream()
            .map(JobExecuteDTO::getId)
            .collect(Collectors.toSet());

        var jobsIdsPendentes = new LinkedList<Long>(jobsIds);
        jobsIdsPendentes.removeAll(inputsJobsIds);
        var inputsJobsIdsPendentes = new LinkedList<Long>(inputsJobsIds);
        inputsJobsIdsPendentes.removeAll(jobsIds);

        if(!jobsIdsPendentes.isEmpty()) {
            var pendentes = "Jobs da Pipeline não declarados: " + jobsIdsPendentes
                .parallelStream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            log.warn(pendentes);
            inconformidades.add(pendentes);
        }
        if(!inputsJobsIdsPendentes.isEmpty()) {
            var pendentes = "Jobs declarados não encontrados na Pipeline: " + inputsJobsIdsPendentes
                .parallelStream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
            log.warn(pendentes);
            inconformidades.add(pendentes);
        }
        // ------------- VALIDANDO INPUT MASSAS -------------
        log.info("Obtendo as Massas declaradas para validar pendências.");
        var massasPendentes = new ArrayList<String>();
        var massasBanco = massaService.findByClienteIdAndNomes(cliente.getId(), massasNome)
            .parallelStream()
            .map(MassaTabela::toDto)
            .toList();

        massasNome.parallelStream()
            .filter(nome ->  massasBanco
                .parallelStream()
                .noneMatch(massa -> massa.getNome().equals(nome)))
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

        // ------------- PREPARANDO DADOS -------------
        var jobsPreparados = JobPreparation.match(jobs, inputJobs);
        var tables = new HashSet<String>();
        var columns = new HashSet<String>();
        inputQueries.parallelStream()
            .map(SqlSintaxe::analyse)
            .forEach(extraction -> {
                tables.addAll(extraction.tables());
                extraction.filters()
                    .parallelStream()
                    .map(QueryFilter::column)
                    .forEach(columns::add);
            });
        massasBanco.parallelStream().forEach(massa -> {
            tables.add(massa.getNome());
            massa.getColunas()
                .parallelStream()
                .map(MassaColunaDTO::getNome)
                .forEach(columns::add);
        });
        var metadadosBanco = ambienteService.getMetadatasFromTables(tables, columns, ambiente);

        log.info("Metadados obtidos no banco:");
        metadadosBanco.stream()
            .peek(tabelaDb -> log.info(tabelaDb.toString()))
            .parallel()
            .forEach(
                tableDb -> massasBanco.parallelStream().forEach(
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
            .parallelStream()
            .filter(variavel -> variavel.getValue().matches("^\\$[^.]*\\..*"))
            .forEach(variavel -> {
                log.info(" - Identificada Variável Global solicitando Massa: {}", variavel);
                var campoArray = variavel.getValue().split("\\.");
                var tabelaNome = campoArray[0].substring(1);
                var colunaNome = campoArray[1];
                massasPreparadas.parallelStream()
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
            .parallelStream()
            .peek(variavel -> log.info(variavel.toString()))
            .map(Map.Entry::getValue)
            .filter(FormatString::possuiVariaveis)
            .collect(Collectors.joining(", "));
        if(!variaveisPendentes.isBlank())
            throw new ValidationException("Variáveis pendentes: " + variaveisPendentes);

        log.info("Aplicando variáveis globais nos Jobs unificados.");
        jobsPreparados.parallelStream().forEach(
            job -> job.aplicarConfiguracoes(execDto.getConfiguracoes())
        );
        log.info("Obtendo queries das Massas.");
//        var massaInsert = new ArrayList<String>();
//        var massaDelete = new ArrayList<String>();
        massasPreparadas.forEach(massa -> {
//            massaInsert.add(massa.gerarQueryInsert());
//            massaDelete.add(massa.gerarQueryDelete());
            var sql = massa.gerarQueryInsert();
            FormatString.substituirVariaveis(sql, massa.getColunasSqlSintaxe());
            log.info(sql);
            sql = massa.gerarQueryDelete();
            FormatString.substituirVariaveis(sql, massa.getColunasSqlSintaxe());
            log.info(sql);
        });

        log.info("Estado final das Job Queries:");
        var jobQueries = jobsPreparados.parallelStream()
            .map(JobPreparation::jobInputs)
            .map(JobExecuteDTO::getQueries)
            .flatMap(Set::parallelStream)
            .peek(log::info)
            .collect(Collectors.toSet());
        ambienteService.validadeQuery(jobQueries, ambiente);

        //Fim
        return run(new PipelinePreparation(
            pipeline,
            ambiente,
            jobsPreparados,
            massasPreparadas
        ));
    }

    public ResponseEntity<RelatorioHistoricoDTO> run(@NonNull PipelinePreparation preparation) {
//        var resumoMassasGeradas = new MasterSummary<MassaPreparada>();
//        try {
//            if(preparation.massas() != null && preparation.massas().size() > 0) {
//                resumoMassasGeradas = massaService.newInserts(
//                    AmbienteAcessoDTO.banco(preparation.ambiente()),
//                    preparation.massas()
//                );
//            }
//            if(resumoMassasGeradas.getStatus() != SummaryStatus.SUCESSO) {
//                var mensagem = new StringBuilder("Erro na geração das Massas.\n");
//                resumoMassasGeradas.getFailed().forEach(
//                    (obj, erro) -> mensagem
//                        .append(obj.getTabela())
//                        .append(": ")
//                        .append(erro)
//                        .append("\n")
//                );
//                throw new RuntimeException(mensagem.toString());
//            }

        //DESENVOLVIMENTO:
//        log.info(preparation.toString());
        return ResponseEntity.ok(null);

        //PRODUÇÃO:
//            val jobsProcessados = jobService.executar(
//                preparation.ambiente().acessoBanco(),
//                preparation.ambiente().acessoFtp(),
//                preparation.jobs()
//            );
//            val evidencias = evidenciaService.gerarEvidencia(jobsProcessados);
//            val relatorio = relatorioService.buildAndPersist(preparation, evidencias);
//            val relatorioHistorico = new RelatorioHistoricoDTO(relatorio);
//            return ResponseEntity.ok(relatorioHistorico);



//        }
//        finally {
//            log.info("Deletando Massas salvas");
//           massaService.delete(
//               preparation.ambiente(),
//               resumoMassasGeradas.getSaved()
//           );
//        }
    }

    @PostMapping(value = "new")
    public ResponseEntity<?> createNew(@RequestBody PipelineInfoDTO dto)
    throws DuplicatedRecordException {
        pipelineService.checkDuplicated(dto.getNome(), dto.getClienteId());
        val cliente = clienteService.findById(dto.getClienteId());
        val jobs = jobService.findByClienteAndNome(cliente, dto.getJobs());
        val pipeline = Pipeline.parseInfoDto(dto, jobs, cliente);
        pipelineService.persist(pipeline);

        dto.setJobs(
            jobs.stream().map(Job::getNome).toList()
        );
        log.info("Resposta ao cliente:");
        log.info(dto.toString());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "update")
    public ResponseEntity<String> update(@RequestBody PipelineInfoDTO pipeline) {
        if(getAndUpdate(pipeline).isPresent())
            return ResponseEntity.ok("Pipeline atualizada");

        val mensagem = String.format("Pipeline %s não encontrada no banco para Cliente ID %d",
            pipeline.getNome(), pipeline.getClienteId());
        throw new NoSuchElementException(mensagem);
    }

//    public Pipeline createNewPipeline(@NonNull PipelineExecDTO execDTO) {
//        log.info("Criando nova Pipeline '{}' para Cliente ID {}.",
//            execDTO.getPipelineId().getNome(), execDTO.getClienteId());
//        val cliente = clienteService.findById(execDTO.getClienteId());
//
//        log.info("Obtendo todos os Jobs listados no DTO.");
//        val jobIds = execDTO.getJobs()
//            .stream()
//            .map(JobExecuteDTO::getId)
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

    public Optional<Pipeline> getAndUpdate(@NonNull PipelineInfoDTO dto) {
        val pipeline = pipelineService.getUniqueOne(dto.getNome(), dto.getClienteId());
        if(pipeline.isEmpty()) return pipeline;

        val pipelineBanco = pipeline.get();
        val cliente = pipelineBanco.getCliente();
        log.info("Comparando Pipelines.");
        log.info("Pipelines Usuário: {}", dto);
        log.info("Pipelines Banco: {}", pipelineBanco);
        val atualizarDescricao = pipelineBanco.atualizarDescricao(dto.getDescricao());
        val atualizarJobs = pipelineBanco.atualizarJobs(dto.getJobs());

        if(atualizarDescricao || atualizarJobs) {
            log.info("A Pipeline precisa ser atualizada.");
            if(atualizarDescricao)
                pipelineBanco.setDescricao(dto.getDescricao());
            if(atualizarJobs)
                pipelineBanco.setJobs(jobService.findByClienteAndNome(cliente, dto.getJobs()));
            pipelineService.persist(pipelineBanco);
        }
        return pipeline;
    }

    @DeleteMapping(value = "clientId/{clientId}/pipeline/{name}")
    public ResponseEntity<String> delete(
        @PathVariable(name = "clientId") Long clientId,
        @PathVariable(name = "name") String nome) {
        //----------------------------------------
        var pipeline = pipelineService.getUniqueOne(nome, clientId);
        if (pipeline.isPresent()) {
            pipeline.get().setOcultar(true);
            pipelineService.persist(pipeline.get());
        }
        else {
            return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body("Pipeline '" + nome + "' não encontrada.");
        }
        return ResponseEntity.ok("Pipeline '" + nome + "' deletada com sucesso.");
    }

}
