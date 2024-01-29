package br.com.ppw.dma.job;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.net.DownloadManager;
import br.com.ppw.dma.system.ExcelXLSX;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import static br.com.ppw.dma.system.ExitCodes.SUCCESS;
import static br.com.ppw.dma.util.FormatString.*;

@Service
@Slf4j
public class JobService extends MasterService<Long, Job, JobService> {

    @Autowired
    private Gson gson;

    //TODO: remover daqui e reorganizar
    @Autowired
    private final EvidenciaService evidenciaSerive;

    @Autowired
    private final JobRepository dao;

    private ConectorSftp sftp;

    public static final DateTimeFormatter CONVERSOR_DATA_SCHEDULE = DateTimeFormatter
        .ofPattern("dd/MM/yyyy");


    public JobService(JobRepository dao, EvidenciaService evidenciaSerive) {
        super(dao);
        this.dao = dao; //TODO: precisa mesmo?
        this.evidenciaSerive = evidenciaSerive;
    }

    public List<Job> findByClienteAndNome(@NonNull Cliente cliente, @NonNull List<String> nomes) {
        return dao.findByClienteAndNomeIn(cliente, nomes);
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
            .map(Job -> refinarCampos(Job, planilhaNome, arquivoNome))
            .collect(Collectors.toList());
    }

    // TODO: javadoc
    private JobInfoDTO refinarCampos(
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

    //TODO: javadoc
    public List<JobExecutePOJO> executarJobs(
        @NonNull ConectorSftp sftp,
        @NonNull AmbienteAcessoDTO banco,
        @NonNull List<JobExecuteDTO> jobsDto) {
        //--------------------------------------
        //TODO: separar a Evidência do Resumo, onde o Resumo contêm as informações da execução
        //  (aonde teve sucesso e aonde teve falha)
        //Será convertido o JobExecuteDTO para JobExecutePOJO, inserindo nele a entidade Job via ID.
        //Também será gerado JobInfoDTO com base na entidade. DTO responsável pela execução dos comandos
        //de forma correta e organizada no terminal SFTP
        log.info("Iniciando rotina da execução de Jobs");
        this.sftp = sftp;
        val jobsPojo = jobsDto.stream()
            .map(this::createPojo)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .peek(pojo -> pojo.setBanco(banco))
            .map(this::executarJobPojo)
            .toList();
        log.info("Total de Jobs executados: {}.", jobsPojo.size());

        val sucessos = jobsPojo.stream()
            .filter(JobExecutePOJO::isSucesso)
            .toList()
            .size();
        log.info("Total de Jobs com sucesso: {}.", sucessos);

        return jobsPojo;
    }

    //TODO: javadoc
    private JobExecutePOJO executarJobPojo(@NonNull JobExecutePOJO pojo) {
        val jobDto = pojo.getJobInfo();
        val banco = pojo.getBanco();
        val logs = new ArrayList<DownloadManager>();
        pojo.setDataInicio(OffsetDateTime.now());

        try {
            if(!jobDto.getMascaraLog().isEmpty()) {
                log.info("Obtendo log mais recente pré-execução.");
                logs.addAll(sftp.downloadMaisRecentePreJob(jobDto.pathLog()));
            }
            if(!pojo.getTabelas().isEmpty()) {
                log.info("Consultando tabelas pré-execução.");
                pojo.setTabelas(
                    evidenciaSerive.extractTablePreJob(pojo.getTabelas(), banco)
                );
            }
            log.info("Executando Job.");
            val terminalManager = sftp.comando(pojo.comandoShell());

            if(!logs.isEmpty()) {
                log.info("Obtendo log mais recente pós-execução.");
                sftp.downloadMaisRecentePosJob(logs);
            }
            if(!pojo.getTabelas().isEmpty()) {
                log.info("Consultando tabelas pós-execução.");
                pojo.setTabelas(
                    evidenciaSerive.extractTablePosJob(pojo.getTabelas(), banco)
                ) ;
            }
            pojo.setTerminal(terminalManager.getConsoleLog());
            logs.stream()
                //.peek(fl -> log.info("Comparando arquivos para obter o mais recente."))
                .map(DownloadManager::getPostFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(pojo::addLogs);
            pojo.setSucesso(terminalManager.getExitCode() == SUCCESS.code);
            //TODO: retornar não-sucesso para o front
        }
        catch(Exception e) {
            //TODO: melhorar tratamento. É preciso validar e informar:
            // 1. Se o comando foi executado com sucesso
            // 2. Se foi identificado log antes
            // 3. Se foi identificado log depois
            // 4. Se foi baixado log com sucesso
            // 5. Se foi identificado registro no banco antes
            // 6. Se foi identificado registro no banco depois
            // 7. Se o comando SQL foi inválido
            // 8. Se o comando SQL informado com declarações não permitidas (DELETE, DROP, etc)
            log.error("Erro durante execução do Job [{}] {}: {}",
            jobDto.getId(), jobDto.getNome(), e.getMessage());
        }
        pojo.setDataFim(OffsetDateTime.now());
        return pojo;
    }

    //TODO: javadoc
    private Optional<JobExecutePOJO> createPojo(@NonNull JobExecuteDTO dto) {
        try {
            log.info("Buscando registro para Job id {}.", dto.getId());
            val job = findById(dto.getId());
            log.info("Job encontrado:");
            log.info(job.toString());

            log.info("Agrupando objetos dentro do JobExecutePOJO.");
            val jobInfo = JobInfoDTO.converterJob(job);
            val jobPojo = new JobExecutePOJO();
            jobPojo.setJob(job);
            jobPojo.setJobInfo(jobInfo);
            jobPojo.setOrdem(dto.getOrdem());
            jobPojo.setArgumentos(dto.getArgumentos());
            jobPojo.addComandoSql(dto.getQueries());
            //TODO: add cargas!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            log.info(jobPojo.toString());

            return Optional.of(jobPojo);
        }
        catch(Exception e) {
            log.warn("Erro durante preparo do Job id {}: {}.", dto.getId(), e.getMessage());
            return Optional.empty();
        }
    }

}
