package br.com.ppw.dma.job;

import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.net.DownloadManager;
import br.com.ppw.dma.system.ExcelXLSX;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
import java.util.stream.Collectors;

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

    @Getter @Setter
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
    public List<JobExecutePOJO> executarJobs(@NonNull List<JobExecutePOJO> jobsDto) {
        log.info("Iniciando rotina da execução de Jobs");
        val jobsPojo = jobsDto.stream()
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
        val cargas = new ArrayList<File>();
        val saidas = new ArrayList<DownloadManager>();
        pojo.setDataInicio(OffsetDateTime.now());

        try {
            if(!jobDto.getMascaraEntrada().isEmpty()) {
                log.info("Enviando os arquivos de carga a serem usadas na execução.");
                cargas.addAll(
                    sftp.upload(jobDto.getDiretorioEntrada(), pojo.getCargas())
                );
            }
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
            log.info("Obtendo o sha256 do Job.");
            val sha256 = sftp.comando("sha256sum " + pojo.getJobInfo().pathShell() + " | cut -d ' ' -f1")
                .getConsoleLog()
                .stream()
                .findFirst()
                .orElse("");
            pojo.setSha256(sha256);
            log.info(" - sha256 obtido: '{}'.", sha256);

            log.info("Executando Job.");
            val terminalManager = sftp.comando(pojo.comandoShell());
            pojo.setTerminal(terminalManager.getConsoleLog());
            pojo.setExitCode(terminalManager.getExitCode());

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
            if(!jobDto.getMascaraSaida().isEmpty()) {
                log.info("Coletando as saídas geradas pela execução.");
                saidas.addAll(sftp.downloadMaisRecentePreJob(jobDto.pathSaida()));
            }

        }
        //Anexando mensagens de exceções durante execução
        catch(Exception e) {
            log.error("Erro inesperado na execução do Job [{}] '{}'", jobDto.getId(), jobDto.getNome());
            pojo.getErros().add(e.getMessage());
        }
        //Anexando arquivos de log pós-execução e obtendo avisos de inconformidades
        for(val gerenciador : logs) {
            gerenciador.getPostFile().ifPresent(pojo::addLogs);
            pojo.getErros().addAll(gerenciador.getAvisos());
        }
        //Anexando arquivos de saída e avisos de inconformidades
        for(val gerenciador : saidas) {
            gerenciador.getPreFile().ifPresent(pojo::addProdutos);
            pojo.getErros().addAll(gerenciador.getAvisos());
        }
        //Finalizando com a data de encerramento e solicitando análise técnica do resultado
        pojo.setDataFim(OffsetDateTime.now());
        pojo.analisarExecucao();
        return pojo;
    }

}
