package br.com.ppw.dma.job;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ResultadoSql;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.net.RemoteFile;
import br.com.ppw.dma.net.SftpFileManager;
import br.com.ppw.dma.pipeline.PipelinePreparation;
import br.com.ppw.dma.system.ExcelXLSX;
import br.com.ppw.dma.system.FileSystemService;
import br.com.ppw.dma.util.SqlUtils;
import com.google.gson.Gson;
import jakarta.persistence.PersistenceException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.*;

@Service
@Slf4j
public class JobService extends MasterService<Long, Job, JobService> {

    @Autowired
    private Gson gson;

    @Autowired
    private final JobRepository dao;

    @Autowired
    private final FileSystemService fileSystemService;

    @Getter
    private ConectorSftp sftp;

    @Getter
    private AmbienteAcessoDTO banco;

    public static final DateTimeFormatter CONVERSOR_DATA_SCHEDULE = DateTimeFormatter
        .ofPattern("dd/MM/yyyy");


    public JobService(JobRepository dao, FileSystemService fileSystemService) {
        super(dao);
        this.dao = dao; //TODO: precisa mesmo?
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
        val result = dao.findAllByClienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public List<Job> persistAll(List<Job> jobs) {
        return jobs.stream()
            .map(this::persist)
            .collect(Collectors.toList());
    }

    @Transactional
    public Job persist(@NotNull Job job) {
        log.info("Persistindo Job no banco:");
        log.info(job.toString());
        job = dao.save(job);

        log.info("Job ID {} gravado com sucesso.", job.getId());
        return job;
    }

    public ExcelXLSX lerXlsx(@NotNull MultipartFile file) throws IOException {
        val nomeArquivo = file.getOriginalFilename();
        log.info("Nome do arquivo: '{}'.", nomeArquivo);
        val localDir = System.getProperty("user.dir") + File.separator;

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
        return new ExcelXLSX(nomeArquivo, file); //TODO: Criar handler para NoClassDefFoundError ?
    }

    public List<JobInfoDTO> mapExcelToJobInfoDto(
        @NotNull ExcelXLSX excel, @NotBlank String planilhaNome) {
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
        val registro = "Job [" +jobPojo.getId()+ "] " +jobPojo.getNome();
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

    //TODO: criar exception própria
    //TODO: javadoc
//    public List<Evidencia> executarJob(@NonNull PipelinePreparation preparation) {
    public List<JobProcess> executarJob(@NonNull PipelinePreparation preparation) {
        log.debug("Configurando conexão do Banco e do SFTP.");
        banco = AmbienteAcessoDTO.banco(preparation.ambiente());
        sftp = ConectorSftp.conectar(AmbienteAcessoDTO.ftp(preparation.ambiente()));

        //TODO: paliativo. Remover e aprimorar o código
        switch(preparation.ambiente().getConexaoSftp()) {
            case "10.129.226.157:22" -> {
                sftp.getProperties().putAll(ConectorSftp.getVivo1Properties());
                log.info("Adicionando variáveis de ambiente VIVO1.");
            }
            case "10.129.164.206:22" -> {
                sftp.getProperties().putAll(ConectorSftp.getVivo3Properties());
                log.info("Adicionando variáveis de ambiente VIVO3.");
            }
        }

        log.info("Iniciando rotina da execução de Jobs");
        val sucessos = new AtomicInteger();
        val jobProcesses = preparation.jobs()
            .stream()
            .map(this::executarJob)
            .peek(process -> sucessos.addAndGet(process.isSucesso() ? 1 : 0))
            .toList();
        log.info("Total de Jobs executados: {}.", jobProcesses.size());
        log.info("Total de Jobs com sucesso: {}.", sucessos);

        log.debug("Desconfigurando conexão do Banco e do SFTP.");
        sftp = null;
        banco = null;
//        return evidenciaController.gerarEvidencias(jobProcesses);
        return jobProcesses;
    }

    //TODO: javadoc
    private JobProcess executarJob(@NonNull JobPreparation dados) {
        val jobInfo = dados.jobInfo();
        val jobInput = dados.jobInputs();
        val process = new JobProcess();
        final List<SftpFileManager<RemoteFile>> logsPreJob = new ArrayList<>();
        final List<SftpFileManager<RemoteFile>> logsPosJob = new ArrayList<>();

        process.setJobInfo(jobInfo);
        process.setJobInputs(jobInput);
        process.setDataInicio(OffsetDateTime.now());

        try {
            //Coletas pré-execução
            if(!jobInput.getCargas().isEmpty()) {
                log.info("Enviando os arquivos de carga a serem usadas na execução.");
                jobInput.getCargas().stream()
                    .filter((carga -> carga.getNome() != null && !carga.getNome().isBlank()))
                    .map(fileSystemService::store)
                    .map(carga -> sftp.upload(jobInfo.getDiretorioEntrada(), carga))
                    .forEach(process::addCargas);
            }
            if(!jobInfo.getMascaraLog().isEmpty()) {
                log.info("Obtendo log mais recente pré-execução.");
                jobInfo.pathLog().forEach(
                    path -> logsPreJob.add(sftp.downloadMaisRecente(path)));
            }
            if(!jobInput.getQueries().isEmpty()) {
                log.info("Consultando tabelas pré-execução.");
                process.addTabelasPreJob(
                    extractTable(jobInput.getQueries()));
            }
            //Execução
            log.info("Obtendo o sha256 do Job.");
            val sha256 = sftp.comando("sha256sum " + jobInfo.pathShell() + " | cut -d ' ' -f1")
                .getConsoleLog()
                .stream()
                .findFirst()
                .orElse("");
            log.info(" - sha256 obtido: '{}'.", sha256);
            process.setSha256(sha256);

            log.info("Acionando Job remoto.");
            val terminalManager = sftp.comando(dados.comandoShell());
            process.setTerminal(terminalManager.getConsoleLog());
            process.setExitCode(terminalManager.getExitCode());
            process.setSucesso(true);
            log.info("Job acionado com sucesso.");

            //Coletas pós-execução
            if(!jobInfo.getMascaraSaida().isEmpty()) {
                log.info("Coletando as saídas geradas pela execução.");
                process.addProdutos(
                    sftp.downloadMaisRecente(jobInfo.pathSaida()));
            }
            if(!jobInfo.getMascaraLog().isEmpty()) {
                log.info("Obtendo log mais recente pós-execução.");
                jobInfo.pathLog().forEach(
                    path -> logsPosJob.add(sftp.downloadMaisRecente(path)));
            }
            if(!jobInput.getQueries().isEmpty()) {
                log.info("Consultando tabelas pós-execução.");
                process.addTabelasPosJob(
                    extractTable(jobInput.getQueries()));
            }

            //Com base nos logs pré/pós: comparar cenários de duplicidade para então adicionar ao JobProcess
            for(int i = 0; i < logsPreJob.size(); i++) {
                val logPre = logsPreJob.get(i);
                val logPos = logsPosJob.get(i);
                process.addLogs(SftpFileManager.compare(logPre, logPos));
            }
        }
        //Caso erro no acionamento/monitoramento do DetMaker com o Job
        catch(Exception e) {
            log.error("Erro inesperado na execução do Job [{}] '{}'", jobInfo.getId(), jobInfo.getNome());
            process.setErroFatal(e.getMessage());
        }
        //Fim
        process.setDataFim(OffsetDateTime.now());
        return process;
    }

    //TODO: javadoc
    public List<ResultadoSql> extractTable(@NonNull List<? extends ComandoSql> comandosSql) {
        val extracoes = comandosSql.stream().map(cmdSql -> {
            val manager = new ResultadoSql(cmdSql);
            try(val masterDao = new MasterOracleDAO(banco)) {
                val extracao = masterDao.getAllInfoFromTable(manager.getSqlCompleta());
                manager.addResultado(extracao);
            }
            catch(SQLException | PersistenceException e) {
                val errorMessage = SqlUtils.getExceptionMainCause(e);
                log.warn(errorMessage);
                manager.setMensagemErro(errorMessage);
            }
            catch(Exception e) {
                e.printStackTrace();
                manager.setMensagemErro(e.getMessage());
            }
            return manager;
        })
        .toList();

        log.info("Total de comandos SQL realizados: {}.", extracoes.size());
        return extracoes;
    }

}
