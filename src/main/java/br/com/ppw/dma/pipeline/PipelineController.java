package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.cliente.ClienteService;
import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.job.JobExecuteDTO;
import br.com.ppw.dma.job.JobPreparation;
import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.massa.MassaTabela;
import br.com.ppw.dma.massa.MassaTabelaService;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.relatorio.RelatorioService;
import br.com.ppw.dma.system.FileSystemService;
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
    public ResponseEntity<RelatorioHistoricoDTO> validadeToStack(@RequestBody PipelineExecDTO execDto) {
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

        // ------------- METADADOS DAS TABELAS -------------
//        var tabelasParaConsulta = new HashSet<String>();
//        var colunasParaConsulta = new HashSet<String>();

        //Para cada query declarada, extrair nomes das tabelas e column.
//        inputJobs.parallelStream()
//            .map(JobExecuteDTO::getQueries)
//            .flatMap(List::parallelStream)
//            .map(ComandoSql::getSql)
//            .forEach(query -> {
//                var tables = SqlSintaxe.getTablesNameFromQuery(query);
//                tabelasParaConsulta.addAll(tables);
//
//                //Considerar apenas as column que também constam nas Variáveis da Pipeline.
//                var columns = SqlSintaxe.getColumnsNameFromQuery(query);
//                var existeVariavel = execDto.getConfiguracoes()
//                    .entrySet()
//                    .parallelStream()
//                    .anyMatch(variavel -> columns.contains(variavel.getKey()));
//                if(existeVariavel) colunasParaConsulta.addAll(columns);
//            });

        //Para cada Massa solicitada, extrair os nomes das tabelas e column.
//        massasBanco.parallelStream().forEach(massa -> {
//            tabelasParaConsulta.add(massa.getNome());
//            massa.getColunas()
//                .parallelStream()
//                .map(MassaColunaDTO::getNome)
//                .forEach(colunasParaConsulta::add);
//        });

//        log.info("TABELAS A CONSULTAR:");
//        tabelasParaConsulta.forEach(log::info);
//        log.info("COLUNAS A CONSULTAR:");
//        colunasParaConsulta.forEach(log::info);
//
//        var tabelasBanco = ambienteService.getMetadatasFromTables(
//            tabelasParaConsulta,
//            colunasParaConsulta,
//            ambiente
//        );
//        log.info("TABELAS E COLUNAS OBTIDAS NO BANCO:");
//        tabelasBanco.forEach(table -> log.info("{}.{}", table.table(), table.column()));


        // ------------- PREPARANDO DADOS -------------
        //Primeiro atualizando os metadados das column das Massas
//        massasBanco.parallelStream().forEach(
//            tabelaDto -> tabelasBanco.parallelStream().forEach(tabelaDto::atualizar)
//        );
//        log.info("Gerando Massas solicitadas.");
//        var massasPreparadas = GeradorDeMassa.mapearMassa(FormatDate.BRASIL_STYLE, massasBanco);
//        log.info("MASSAS GERADAS:");
//        massasPreparadas.forEach(massa -> log.info(massa.toString()));
//
//        log.info("Aplicando valores das Massas nas variáveis globais da Pipeline.");
//        Function<String, Optional<String>> massaSqlSintaxe = (input) -> {
//            var campoArray = input.split("\\.");
//            var tabelaNome = campoArray[0].substring(1);
//            var colunaNome = campoArray[1];
//            return massasPreparadas.parallelStream()
//                .filter(tabelaDb -> tabelaDb.getTabela().equalsIgnoreCase(tabelaNome))
//                .findFirst()
//                .flatMap(tabelaDb -> tabelaDb.getColunasSqlSintaxe(colunaNome));
//        };
//        execDto.getConfiguracoes().entrySet().parallelStream().forEach(
//            variavel -> {
//                log.info(" - Identificada Variável Global de massa: {}", variavel);
//                if(variavel.getValue().matches("^\\$[^.]*\\..*")) {
//                    massaSqlSintaxe
//                        .apply(variavel.getValue())
//                        .ifPresent(variavel::setValue);
//                }
//                else {
//                    variavel.getValue()
//                }
//
//                var campoArray = variavel.getValue().split("\\.");
//                var tabelaNome = campoArray[0].substring(1);
//                var colunaNome = campoArray[1];
//                massasPreparadas.parallelStream()
//                    .filter(tabelaDb -> tabelaDb.getTabela().equalsIgnoreCase(tabelaNome))
//                    .findFirst()
//                    .flatMap(tabelaDb -> tabelaDb.getColunasSqlSintaxe(colunaNome))
//                    .ifPresent(valor -> {
//                        log.info(" - Novo valor: {}", valor);
//                        variavel.setValue(valor);
//                    });
//            });
        var jobsPreparados = JobPreparation.match(jobs, inputJobs);
        var metadadosBanco = ambienteService.getMetadatasFromQueries(inputQueries, ambiente);

        log.info("Metadados obtidos no banco:");
        metadadosBanco.stream()
            .peek(tabelaDb -> log.info(tabelaDb.toString()))
            .parallel()
            .forEach(
                tableDb -> massasBanco.parallelStream().forEach(
                    massa -> massa.atualizar(tableDb)
            ));

        //TODO: o formato de data deveria estar em Ambiente.bancoDataFormato ou Global.bancoDataFormato.
        log.info("Gerando Massas com dados atualizados.");
        var massasPreparadas = GeradorDeMassa.mapearMassa(FormatDate.BRASIL_STYLE, massasBanco);
        log.info("Massa gerada:");
        massasPreparadas.forEach(massa -> log.info(massa.toString()));

        // ------------- DEFININDO VARIÁVEIS -------------
        //TODO:
        // 1. Após coletado os metadados das tabelas, tentar aplicar conversão dos job-exec.
        //   - Preciso de um DateTimeFormatter geral.
        //   - Preciso do TipoColuna de cada coluna.
        // 2. Já validar se a SQL final é segura e somente de consulta.
        // * Mover parte desse método para novo endpoint, focado na validação das solicitações no DTO.
        // * Adicionar padrão da data em Ambiente.bancoDataFormato ou Global.bancoDataFormato.
        log.info("Aplicando valores das Massas nas variáveis globais da Pipeline.");
        execDto.getConfiguracoes()
            .entrySet()
            .parallelStream()
            .filter(variavel -> variavel.getValue().matches("^\\$[^.]*\\..*"))
            .forEach(variavel -> {
                log.info(" - Identificada Variável Global de massa: {}", variavel);
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
        execDto.getConfiguracoes().entrySet().forEach(variavel -> log.info(variavel.toString()));

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
            execDto.getAtividade(),
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
