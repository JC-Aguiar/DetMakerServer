package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.jobQuery.ResultadoSql;
import br.com.ppw.dma.domain.master.MasterOracleDAO;
import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.queue.QueuePayloadJob;
import br.com.ppw.dma.domain.queue.QueuePayloadJobCarga;
import br.com.ppw.dma.domain.queue.QueuePayloadQuery;
import br.com.ppw.dma.domain.queue.result.JobProcess;
import br.com.ppw.dma.domain.storage.ExcelXlsx;
import br.com.ppw.dma.domain.storage.FileSystemService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.net.SftpFileManager;
import br.com.ppw.dma.util.FormatDate;
import br.com.ppware.api.TipoColuna;
import jakarta.persistence.PersistenceException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static br.com.ppw.dma.util.FormatString.*;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Service
@FieldDefaults(level = PRIVATE)
public class JobService extends MasterService<Long, Job, JobService> {

    final ModelMapper mapper;
    final ModelMapper mapperFlex;
    final JobRepository dao;
    final JobParameterRepository jobParameterRepository;
    final JobResourceRepository jobResourceRepository;
    final FileSystemService fileSystemService;


    @Autowired
    public JobService(
        @Qualifier("Basic") ModelMapper mapper,
        @Qualifier("Flex") ModelMapper mapperFlex,
        JobRepository dao,
        JobParameterRepository jobParameterRepository,
        JobResourceRepository jobResourceRepository,
        FileSystemService fileSystemService)
    {
        super(dao);
        this.mapper = mapper;
        this.mapperFlex = mapperFlex;
        this.dao = dao;
        this.jobParameterRepository = jobParameterRepository;
        this.jobResourceRepository = jobResourceRepository;
        this.fileSystemService = fileSystemService;
    }

    public List<Job> findByClienteAndNome(@NonNull Cliente cliente, @NonNull List<String> nomes) {
        log.info("Procurando Jobs do Cliente '{}' para os seguintes nomes: {}.",
            cliente.getNome(), String.join(", ", nomes));
        val jobs = dao.findByClienteAndNomeIn(cliente, nomes);
        log.info("Total de Jobs encontrados: {}", jobs.size());
        return jobs;
    }

    public List<Job> findAllByCliente(@NonNull Long clienteId) {
        var ids = dao.findIdAndNomeByClienteId(clienteId)
            .stream()
            .map(JobReferenceProjection::getId)
            .collect(Collectors.toSet());
        if(ids.isEmpty()) throw new NoSuchElementException();
        return getJobsFullyFetched(ids);
    }

    public Job getJobFullyFetched(Long id) {
        var futureJob = dao.asyncOneById(id);
        var futureParams = jobParameterRepository.asyncAllByJobId(id);
        var futureResources = jobResourceRepository.asyncAllByJobId(id);
        CompletableFuture.allOf(futureJob, futureParams, futureResources).join();

        try {
            var job = futureJob.get().orElseThrow();
            var parameters = futureParams.get();
            var resources = futureResources.get();
            var cargas = new ArrayList<JobResource>();
            var logs = new ArrayList<JobResource>();
            var remessas = new ArrayList<JobResource>();

            resources.forEach(resource -> {
                switch(resource.getTipo()) {
                    case CARGA -> cargas.add(resource);
                    case LOG -> logs.add(resource);
                    case REMESSA -> remessas.add(resource);
                }
            });
            job.setParametros(parameters);
            job.setCargas(cargas);
            job.setLogs(logs);
            job.setRemessas(remessas);
            return job;
        }
        catch (InterruptedException | CancellationException | ExecutionException e) {
            throw new RuntimeException("Erro ao consultar e mesclar dados dos Jobs assincronamente.", e);
        }
    }

    public List<Job> getJobsFullyFetched(Set<Long> ids) {
        var futureJobs = dao.asyncAllById(ids);
        var futureParams = jobParameterRepository.asyncAllByJobId(ids);
        var futureResources = jobResourceRepository.asyncAllByJobId(ids);
        CompletableFuture.allOf(futureJobs, futureParams, futureResources).join();

        try {
            // Agrupar JobParameters e JobResources por Job
            var jobs = futureJobs.get();
            var parametersByJobId = futureParams.get().stream()
                .collect(Collectors.groupingBy(param -> param.getJob().getId()));
            var resourcesByJobId = futureResources.get().stream()
                .collect(Collectors.groupingBy(resource -> resource.getJob().getId()));

            // Mapeando
            jobs.forEach(job -> {
                var params = parametersByJobId.getOrDefault(job.getId(), new ArrayList<>());
                var cargas = new ArrayList<JobResource>();
                var logs = new ArrayList<JobResource>();
                var remessas = new ArrayList<JobResource>();
                resourcesByJobId.getOrDefault(job.getId(), new ArrayList<>()).forEach(resource -> {
                    switch(resource.getTipo()) {
                        case CARGA -> cargas.add(resource);
                        case LOG -> logs.add(resource);
                        case REMESSA -> remessas.add(resource);
                    }
                });
                job.setParametros(params);
                job.setCargas(cargas);
                job.setLogs(logs);
                job.setRemessas(remessas);
            });
            return jobs;
        }
        catch (InterruptedException | CancellationException | ExecutionException e) {
            throw new RuntimeException("Erro ao consultar e mesclar dados dos Jobs assincronamente.", e);
        }
    }

    public ExcelXlsx lerXlsx(@NotNull MultipartFile file) throws IOException {
        val nomeArquivo = file.getOriginalFilename();
        log.info("Nome do arquivo: '{}'.", nomeArquivo);

        log.debug("Validando extensão do arquivo.");
        val extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1);
        if(!extensao.equals("xlsx"))
            throw new RuntimeException("Arquivo inválido. O arquivo precisa ser no formato '.xlsx'");
        log.debug("Extensão do arquivo validada com sucesso.");

        //log.info("Salvando arquivo localmente: '{}'.", localDir + DIR_RECURSOS);
        //val arquivoDestino = new File(localDir + DIR_RECURSOS, nomeArquivo);
        //file.transferTo(arquivoDestino); //TODO: Criar handler para IOException?
        log.debug("Arquivo salvo com sucesso.");

        log.info("Abrindo Schedule.");
        return new ExcelXlsx(nomeArquivo, file); //TODO: Criar handler para NoClassDefFoundError ?
    }

    public List<JobInfoDTO> mapExcelToJobInfoDto(
        @NotNull ExcelXlsx excel, @NotBlank String planilhaNome) {
        //--------------------------------------------------------
        val arquivoNome = excel.getNomeArquivo();
        log.info("Tentando ler registros da planilha '{}'.", planilhaNome);
        return excel.getPlanilhas()
            .stream()
            .filter(planilha -> planilha.getNome().equals(planilhaNome))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Planilha " + planilhaNome + " não encontrada."))
            .getJobsPOJO()
            .stream()
            .map(Job -> converterScheduleJobEmInfo(Job, planilhaNome, arquivoNome))
            .collect(Collectors.toList());
    }

    // TODO: javadoc
    //TODO: mover para escopo do JobSchedulePOJO como um construtor alternativo
    private JobInfoDTO converterScheduleJobEmInfo(
        @NotNull JobSchedulePOJO jobPojo,
        @NotBlank String planilhaNome,
        @NotBlank String arquivoNome)
    {
        val registro = "Job " +jobPojo.getNome()+ "[" +jobPojo.getId()+ "] ";

        log.info("{}: Validando os campos possuem conteúdo de fato ou apenas indicadores vazios.", registro);
        val tabelas = valorVazio(jobPojo.getTabelas().replace("RCVRY.", ""));
        val parametroNome = valorVazio(jobPojo.getParametros());
        val parametroDescricao = valorVazio(jobPojo.getDescricaoParametros());
        val diretorioEntrada = valorVazio(jobPojo.getDiretorioEntrada());
        val mascaraEntrada = valorVazio(jobPojo.getMascaraEntrada());
        val diretorioSaida = valorVazio(jobPojo.getDiretorioSaida());
        val mascaraSaida = valorVazio(jobPojo.getMascaraSaida());
        val diretorioLog = valorVazio(jobPojo.getDiretorioLog());
        val mascaraLog = valorVazio(jobPojo.getMascaraLog());

        log.info("{}: Detectando se há separadores no texto para dividi-los em listas.", registro);
        val listaTabelas = dividirValores(tabelas);
        val listaParametrosNome = dividirValores(parametroNome);
        val listaParametrosDescricao = dividirValores(parametroDescricao);
        val listaMascarasEntrada = dividirValores(mascaraEntrada);
        val listaMascarasSaida = dividirValores(mascaraSaida);
        val listaMascarasLog = dividirValores(mascaraLog);

        //Mensagem em caso de divergência
        if(listaParametrosNome.size() != listaParametrosDescricao.size()){
            log.warn("ATENÇÃO: Existe uma divergência na planilha entre 'Parâmetros' x 'Descrição'.");
            log.warn("ATENÇÃO: 'Parâmetros' têm {} linhas e 'Descrição' têm {}.",
                listaParametrosNome.size(), listaParametrosDescricao.size());
            log.warn("ATENÇÃO: A aplicação usará a quantidade na coluna 'Parâmetros' e, portanto, " +
                "não haverá descrição de todos os parâmetros para auxiliar o preenchimento.");
        }
        log.info("{}: Gerando JobInfoDTO.", registro);
        val JobDto = new ModelMapper().map(jobPojo, JobInfoDTO.class);
        JobDto.setId(null);

        var size = Math.min(listaParametrosNome.size(), listaParametrosDescricao.size());
        for(var i = 0; i < size; i++) {
            JobDto.getParametros().add(
                JobParamDTO.builder()
                    .tipo(TipoColuna.UNSET.name())
                    .nome(listaParametrosNome.get(i))
                    .descricao(listaParametrosDescricao.get(i))
                    .build()
            );
        }
        listaMascarasEntrada.stream()
            .map(mask -> JobResourceDTO.builder()
                .tipo(JobResourceType.CARGA.name())
                .endereco(Path.of(diretorioEntrada, mask).toAbsolutePath().toString())
                .protocolo(JobResourceProtocolType.FTP.name())
                .build())
            .forEach(dto -> JobDto.getMascaraEntrada().add(dto));

        listaMascarasLog.stream()
            .map(mask -> JobResourceDTO.builder()
                .tipo(JobResourceType.LOG.name())
                .endereco(Path.of(diretorioEntrada, mask).toAbsolutePath().toString())
                .protocolo(JobResourceProtocolType.FTP.name())
                .build())
            .forEach(dto -> JobDto.getMascaraLog().add(dto));

        listaMascarasSaida.stream()
            .map(mask -> JobResourceDTO.builder()
                .tipo(JobResourceType.REMESSA.name())
                .endereco(Path.of(diretorioEntrada, mask).toAbsolutePath().toString())
                .protocolo(JobResourceProtocolType.FTP.name())
                .build())
            .forEach(dto -> JobDto.getMascaraSaida().add(dto));

        try {
            log.info("{}: Tentando converter 'Data de Atualização' da planilha para o DTO.", registro);
            val data = refinarCelula(jobPojo.getDataAtualizacao());
            JobDto.setDataAtualizacao(LocalDate.parse(data, FormatDate.BRASIL_SIMPLE_STYLE));
            log.info("{}: Conversão realizada com sucesso.", registro);
        }
        catch(Exception e) {
            log.info("{}: Não foi possível converter a data: {}.", registro, e.getMessage());
        }
        log.info("{}: {}", registro, JobDto);
        return JobDto;
    }

    //TODO: criar exception própria?
    //TODO: mover para TaskQueue?
    //TODO: javadoc
//    public List<Evidencia> executarJob(@NonNull QueuePayload preparation) {
    @Transactional(noRollbackFor = Throwable.class)
    public List<JobProcess> executar(
        @NonNull AmbienteAcessoDTO conexaoBanco,
        @NonNull AmbienteAcessoDTO conexaoSftp,
        @NonNull List<QueuePayloadJob> jobs)
    {
        log.info("Iniciando rotina da execução de Jobs");
        val jobProcesses = jobs.stream()
            .map(job -> new JobProcess(conexaoBanco, conexaoSftp, job))
            .map(this::executar)
            .toList();
        val sucessos = jobProcesses.stream().filter(JobProcess::isSucesso).count();
        log.info("Total de Jobs com sucesso: {}/{}.", sucessos, jobProcesses.size());
        return jobProcesses;
    }

    //TODO: javadoc
    private JobProcess executar(JobProcess process) {
        try {
            var cargasEnvio = process.getCargasEnvio();
            var cargasConsulta = process.getCargasMascara();
            var queriesConsulta = process.getQueriesExec();
            var logsConsulta = process.getLogsMascara();
            var remessasConsulta = process.getRemessasMascara();
            var conexaoBanco = process.getConexaoBanco();

            log.debug("Configurando conexão do Banco e do SFTP.");
            var sftp = ConectorSftp.conectar(process.getConexaoSftp());

            //TODO: paliativo. Remover quando obtida uma solução em produção
            switch(sftp.getServer()) {
                case "${DETMAKER_HOST_1}" -> ConectorSftp.setVivo1Properties(sftp);
                case "${DETMAKER_HOST_2}" -> ConectorSftp.setVivo2Properties(sftp);
                case "${DETMAKER_HOST_3}" -> ConectorSftp.setVivo3Properties(sftp);
            }
            //Coletas pré-execução
            if(!cargasEnvio.isEmpty()) {
                log.info("Enviando os arquivos de carga a serem usados na execução.");
                cargasEnvio.stream()
                    .filter(QueuePayloadJobCarga::validName)
                    .map(carga -> {
                        var file = fileSystemService.store(carga);
                        return sftp.upload(carga.getDiretorio(), file);
                    })
                    .filter(SftpFileManager::isSuccess)
                    .forEach(carga -> process.getCargasEnviadas().add(carga));
            }
            if(!cargasConsulta.isEmpty()) {
                log.info("Coletando os arquivos de carga que serão usados na execução.");
                cargasConsulta.forEach(mascara -> process.addCargas(
                    sftp.downloadAll(mascara)
                ));
            }
            if(!queriesConsulta.isEmpty()) {
                log.info("Consultando tabelas pré-execução.");
                process.addTabelasPreJob(
                    extractTable(conexaoBanco, queriesConsulta)
                );
            }
            //Execução
            log.info("Obtendo o sha256 do Job.");
            val sha256 = sftp.comando(process.getComandoVersao())
                .getConsoleLog().stream()
                .findFirst()
                .orElse("");
            log.info(" - sha256 obtido: '{}'.", sha256);
            process.setVersao(sha256);

            log.info("Acionando Job remoto.");
            var dataAgora = LocalDateTime.now(RELOGIO);
            val terminalManager = sftp.comando(process.getComandoExec());
//            process.setTerminal(terminalManager);
            process.setExitCode(terminalManager.getExitCode());
            process.setSucesso(true);
            log.info("Job acionado com sucesso.");

            //Coletas pós-execução
            if(!remessasConsulta.isEmpty()) {
                log.info("Coletando as saídas geradas pela execução.");
                remessasConsulta.forEach(mascara -> process.addRemessas(
                    sftp.downloadAllRecente(mascara, dataAgora)
                ));
            }
            if(!logsConsulta.isEmpty()) {
                log.info("Obtendo log mais recente pós-execução.");
                logsConsulta.forEach(mascara -> process.addLogs(
                    sftp.downloadAllRecente(mascara, dataAgora)
                ));
            }
            if(!queriesConsulta.isEmpty()) {
                log.info("Consultando tabelas pós-execução.");
                process.addTabelasPosJob(
                    extractTable(conexaoBanco, queriesConsulta)
                );
            }
        }
        //Caso exception no acionamento/monitoramento do DetMaker com o Job
        catch(Exception e) {
            e.printStackTrace();
            log.error("Erro inesperado na execução do {}º Job ['{}']: {}",
                process.getOrdem(),
                process.getNome(),
                e.getMessage());
            process.setErroFatal(e.getMessage());
        }
        process.setDataFim(OffsetDateTime.now());
        return process;
    }

    //TODO: javadoc
    public List<ResultadoSql> extractTable(AmbienteAcessoDTO conexaoBanco, List<QueuePayloadQuery> jobQuery) {
        var sucessos = new AtomicInteger(0);
        val extracoes = jobQuery.stream().map(query -> {
            val resultado = new ResultadoSql(
                query.getNome(),
                query.getDescricao(),
                query.getQuery());
            try(val masterDao = new MasterOracleDAO(conexaoBanco)) {
                var extracao = masterDao.collectData(query.getQuery());
                resultado.addResultado(extracao);
                sucessos.getAndIncrement();
            }
            catch(SQLException | PersistenceException e) {
                log.warn(e.getMessage());
                val errorMessage = SqlSintaxe.getExceptionMainCause(e);
                resultado.setMensagemErro(errorMessage);
            }
            catch(Exception e) {
                e.printStackTrace();
                resultado.setMensagemErro(e.getMessage());
            }
            return resultado;
        })
        .toList();

        log.info("Total de queries executadas com sucesso: {}/{}.", sucessos, extracoes.size());
        return extracoes;
    }

    /**
     * Converte os dados enviados pelo usuário num novo {@link Job}. Este processo também persistirá
     * os respectivos dados referentes ao {@link JobParameter} (parâmetros) e {@link JobResource} {carga/log/remessa}.
     * @param cliente {@link Cliente}
     * @param dtos {@link Collection} de {@link JobInfoDTO}
     * @return {@link Job} salvo no banco.
     */

}
