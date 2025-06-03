package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.jobQuery.ResultadoSql;
import br.com.ppw.dma.domain.master.MasterOracleDAO;
import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.storage.ExcelXlsx;
import br.com.ppw.dma.domain.storage.FileSystemService;
import br.com.ppw.dma.domain.task.TaskPayloadJob;
import br.com.ppw.dma.domain.task.TaskPayloadQuery;
import br.com.ppw.dma.domain.task.result.JobProcess;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.net.SftpFileManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static br.com.ppw.dma.util.FormatString.*;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Service
@FieldDefaults(level = PRIVATE)
public class JobService extends MasterService<Long, Job, JobService> {

    final JobRepository dao;
    final FileSystemService fileSystemService;

    public static final DateTimeFormatter CONVERSOR_DATA_SCHEDULE = DateTimeFormatter
        .ofPattern("dd/MM/yyyy");


    @Autowired
    public JobService(JobRepository dao, FileSystemService fileSystemService) {
        super(dao);
        this.dao = dao;
        this.fileSystemService = fileSystemService;
    }

    public Optional<Job> findByClienteAndNome(@NonNull Cliente cliente, String nome) {
        log.info("Procurando no Cliente {} pelo Job {}.", cliente.getNome(), nome);
        return dao.findByClienteAndNome(cliente, nome);
    }

    public List<Job> findByClienteAndNome(@NonNull Cliente cliente, @NonNull List<String> nomes) {
        log.info("Procurando no Cliente {} para os Jobs: {}.",
            cliente.getNome(),
            String.join(", ", nomes)
        );
        val jobs = dao.findByClienteAndNomeIn(cliente, nomes);
        log.info("Total de Jobs encontrados: {}", jobs.size());
        return jobs;
    }

    public List<Job> findAllByCliente(@NonNull Long clienteId) {
        val result = dao.findAllByClienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    @Transactional
    public Job persist(@NotNull Job job) {
        log.info("Persistindo Job no banco:");
        log.info(job.toString());
        job = dao.save(job);

        log.info("Job ID {} gravado com sucesso.", job.getId());
        return job;
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
        @NotNull JobSchedulePOJO jobPojo, @NotBlank String planilhaNome, @NotBlank String arquivoNome) {
        //---------------------------------------------------------------------------------------------
        val registro = "Job '%s' [%d] ".formatted(jobPojo.getNome(), jobPojo.getId());
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
        val listaExecPosJob = dividirValores(jobPojo.getExecutarAposJob());
        val listaPrograma = dividirValores(jobPojo.getPrograma());
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
        JobDto.setDiretorioEntrada(diretorioEntrada);
        JobDto.setDiretorioSaida(diretorioSaida);
        JobDto.setDiretorioLog(diretorioLog);
        JobDto.setExecutarAposJob(listaExecPosJob);
        JobDto.setPrograma(listaPrograma);
        JobDto.setTabelas(listaTabelas);
        JobDto.setParametros(listaParametrosNome);
        JobDto.setDescricaoParametros(listaParametrosDescricao);
        JobDto.setMascaraEntrada(listaMascarasEntrada);
        JobDto.setMascaraSaida(listaMascarasSaida);
        JobDto.setMascaraLog(listaMascarasLog);
        try {
            log.info("{}: Tentando converter 'Data de Atualização' da planilha para o DTO.", registro);
            val data = refinarCelula(jobPojo.getDataAtualizacao());
            JobDto.setDataAtualizacao(LocalDate.parse(data, CONVERSOR_DATA_SCHEDULE));
            log.info("{}: Conversão realizada com sucesso.", registro);
        }
        catch(Exception e) {
            log.info("{}: Não foi possível converter a data: {}.", registro, e.getMessage());
        }
        log.info("{}: {}", registro, JobDto);
        return JobDto;
    }

    //TODO: criar exception própria?
    //TODO: mover para RemoteTask?
    //TODO: javadoc
//    public List<Evidencia> executarJob(@NonNull TaskPayload preparation) {
    @Transactional(noRollbackFor = Throwable.class)
    public List<JobProcess> executar(
        @NonNull AmbienteAcessoDTO conexaoBanco,
        @NonNull AmbienteAcessoDTO conexaoSftp,
        @NonNull List<TaskPayloadJob> jobs)
    {
        log.debug("Configurando conexão do Banco e do SFTP.");
        val banco = conexaoBanco;
        val sftp = ConectorSftp.conectar(conexaoSftp);

        //TODO: paliativo. Remover quando obtida uma solução em produção
        switch(sftp.getServer()) {
            case "10.129.226.157" -> ConectorSftp.setVivo1Properties(sftp);
            case "10.42.252.76" -> ConectorSftp.setVivo2Properties(sftp);
            case "10.129.164.206" -> ConectorSftp.setVivo3Properties(sftp);
        }
        log.info("Iniciando rotina da execução de Jobs");
        log.info("Total de Jobs a executar: {}", jobs.size());
        val sucessos = new AtomicInteger();
        val jobProcesses = jobs.stream()
            .sorted(Comparator.comparing(TaskPayloadJob::getOrdem))
            .map(job -> {
                var process = new JobProcess(job, banco, sftp);
                executar(process);
                sucessos.addAndGet(process.isSucesso() ? 1 : 0);
                return process;
            })
            .toList();
        log.info("Total de Jobs executados com sucesso: {}/{}", sucessos, jobProcesses.size());
        return jobProcesses;
    }

    //TODO: javadoc
    //TODO: esse método não deveria estar aqui ou o parâmetro de entrada
    private void executar(@NonNull JobProcess process) {
        val banco = process.getBanco();
        val sftp = process.getSftp();
        log.info("Executando Job[{}]: '{}'", process.getOrdem(), process.getNome());
        try {
            //Coletas pré-execução
            if(!process.getCargasEnvio().isEmpty() && process.getDirCargaEnvio() != null) {
                log.info("Enviando os arquivos de carga a serem usados na execução.");
                process.getCargasEnvio().stream()
                    .filter(carga -> carga.getNome() != null && !carga.getNome().isBlank())
                    .map(fileSystemService::store)
                    .map(carga -> sftp.upload(process.getDirCargaEnvio(), carga))
                    .filter(SftpFileManager::isSuccess)
                    .forEach(carga -> process.getCargasEnviadas().add(carga));
            }
            if(!process.getCargasMascara().isEmpty()) {
                log.info("Coletando os arquivos de carga que serão usados na execução.");
                process.getCargasMascara().forEach(
                    mascara -> process.addCargas(sftp.downloadAll(mascara))
                );
            }
            if(!process.getQueriesExec().isEmpty()) {
                log.info("Consultando tabelas pré-execução.");
                process.addTabelasPreJob(
                    extractTable(banco, process.getQueriesExec())
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
            log.info("Job remoto acionado.");

            //Coletas pós-execução
            if(!process.getRemessasMascara().isEmpty()) {
                log.info("Coletando as saídas geradas pela execução.");
                process.getRemessasMascara().forEach(
                    mascara -> process.addRemessas(sftp.downloadAllRecente(mascara, dataAgora))
                );
            }
            if(!process.getLogsMascara().isEmpty()) {
                log.info("Obtendo log mais recente pós-execução.");
                process.getLogsMascara().forEach(
                    mascara -> process.addLogs(sftp.downloadAllRecente(mascara, dataAgora))
                );
            }
            if(!process.getQueriesExec().isEmpty()) {
                log.info("Consultando tabelas pós-execução.");
                process.addTabelasPosJob(
                    extractTable(banco, process.getQueriesExec())
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
        log.info("Finalizado Job[{}]: '{}'", process.getOrdem(), process.getNome());
    }

    //TODO: javadoc
    public List<ResultadoSql> extractTable(AmbienteAcessoDTO banco, List<TaskPayloadQuery> jobQuery) {
        var sucessos = new AtomicInteger(0);
        val extracoes = jobQuery.stream().map(query -> {
            val resultado = new ResultadoSql(
                query.getNome(),
                query.getDescricao(),
                query.getQuery());
            try(val masterDao = new MasterOracleDAO(banco)) {
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

}
