package br.com.ppw.dma.job;

import br.com.ppw.dma.evidencia.EvidenciaService;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.net.ConectorSftp;
import br.com.ppw.dma.net.FileManager;
import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.system.ExcelXLSX;
import br.com.ppw.dma.util.FormatDate;
import br.com.ppw.dma.configQuery.ResultadoSql;
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
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static br.com.ppw.dma.DetMakerApplication.DIR_RECURSOS;
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

    public Job findByNome(@NotBlank String nome) {
        return dao.findByNome(nome);
    }

    public List<Job> findByNome(@NonNull List<String> nomes) {
        return dao.findByNomeIn(nomes);
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

        log.info("Salvando arquivo localmente: '{}'.", localDir + DIR_RECURSOS);
        val arquivoDestino = new File(localDir + DIR_RECURSOS, nomeArquivo);
        file.transferTo(arquivoDestino); //TODO: Criar handler para IOException
        log.debug("Arquivo salvo com sucesso.");

        log.info("Abrindo Schedule.");
        return new ExcelXLSX(nomeArquivo, arquivoDestino); //TODO: Criar handler para NoClassDefFoundError
    }

    public List<JobInfoDTO> mapExcelToJobDto(
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
    public JobExecutePOJO executarPilha(@NonNull JobExecutePOJO pojo) {
        val jobDto = pojo.getJobInfo();
        final List<FileManager> logsAntes = new ArrayList<>();
        final List<File> logsDepois = new ArrayList<>();
        try {
            pojo.setDataInicio(OffsetDateTime.now());
            log.info("Preparando diretório para evidências do Job id {}.", pojo.getJob().getId());
            val jobNome = jobDto.getNome().split("\\.")[0];
            val path = Arquivos.criarDiretorio(DIR_RECURSOS + jobNome).toPath();

            log.info("Tentando acessar ambiente remoto.");
            sftp = ConectorSftp.conectar("10.129.164.206", 22, "rcvry", "Ppw@1022");
            //TODO: mover o ConectorSftp para outro escopo, a fim de não ser necessário múltiplas instâncias
            //TODO: obter e tratar corretamente o IP, PORTA, USUÁRIO E SENHA

            if(!jobDto.getMascaraLog().isEmpty()) {
                log.info("Obtendo log mais recente pré-execução.");
                logsAntes.addAll(downloadMaisRecente(jobDto, path));
            }
            if(!pojo.getTabelas().isEmpty()) {
                log.info("Consultando tabelas pré-execução.");
                pojo.setTabelas(
                    evidenciaSerive.extractTablePreJob(pojo.getTabelas()));
            }
            log.info("Executando Job.");
            val jobResultado = sftp.comando(pojo.comandoShell());

            log.info("Criando arquivo do log obtido no terminal.");
            val nomeLogTerminal = jobNome + "_terminal_log" + FormatDate.fileNameStyle() + ".txt";
            val conteudoLogTerminal = String.join("\n", jobResultado.getConsoleLog());
            val diretorioLogTerminal = path.toAbsolutePath().toString();
            final File terminalLog = Arquivos.criarEscrever(
                diretorioLogTerminal,
                nomeLogTerminal,
                conteudoLogTerminal
            );
            log.info("Caminho do log do terminal: {}", terminalLog.getAbsolutePath());

            if(!logsAntes.isEmpty()) {
                log.info("Obtendo log mais recente pós-execução");
                logsDepois.addAll(
                    logsAntes.stream()
                        .map(this::downloadMaisRecente)
                        .map(FileManager::latestModified)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList()
                );
            }
            if(!pojo.getTabelas().isEmpty()) {
                log.info("Consultando tabelas pós-execução.");
                pojo.setTabelas(
                    evidenciaSerive.extractTablePosJob(pojo.getTabelas()));
            }
            pojo.addLogs(terminalLog);
            pojo.addLogs(logsDepois);
            pojo.setSucesso(jobResultado.getExitCode() == 0);
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

    private List<ResultadoSql> extrairBanco(List<ResultadoSql> tabelas) {
        return tabelas.stream()
            .map(evidenciaSerive::extractTablePreJob)
            .toList();
    }

    //TODO: Javadoc
    private List<FileManager> downloadMaisRecente(@NonNull JobInfoDTO jobInfo, @NonNull Path path) {
        val logsNome = jobInfo.pathLog().toArray(new String[0]);
        log.info(Arrays.toString(logsNome));

        log.debug("Realizando cada download para cada log informado pelo Job.");
        val arquivosObtidos = sftp.downloadMaisRecente(path, logsNome);
        if(!arquivosObtidos.isEmpty())
            log.info("Total de downloads: {}", arquivosObtidos.size());
        else
            log.warn("Nenhum download realizado com sucesso.");
        return arquivosObtidos;
    }

    private FileManager downloadMaisRecente(@NonNull FileManager fileManager) {
        val logNome = fileManager.getReference();
        log.debug("Consultando por '{}':", logNome);

        log.debug("Realizando cada download para cada log informado pelo Job.");
        val arquivosObtidos = sftp.downloadMaisRecente(fileManager.getPath(), logNome);
        if (!arquivosObtidos.isEmpty()) {
            log.info("Total de downloads: {}", arquivosObtidos.size());
            arquivosObtidos.forEach(fileManager::addFile);
        }
        else log.warn("Nenhum download realizado com sucesso.");
        return fileManager;
    }


}
