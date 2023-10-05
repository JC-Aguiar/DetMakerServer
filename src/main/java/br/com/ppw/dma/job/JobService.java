package br.com.ppw.dma.job;

import br.com.ppw.dma.system.Arquivos;
import br.com.ppw.dma.system.ExcelXLSX;
import br.com.ppw.dma.evidencia.EvidenciaPOJO;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.net.ConectorSftp;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.DetMakerApplication.DIR_RECURSOS;
import static br.com.ppw.dma.util.FormatString.*;

@Service
@Slf4j
public class JobService extends MasterService<Long, Job, JobService> {

    @Autowired
    private Gson gson;

    @Autowired
    private final JobRepository dao;

    private ConectorSftp sftp;

    public static final DateTimeFormatter CONVERSOR_DATA_SCHEDULE = DateTimeFormatter
        .ofPattern("dd/MM/yyyy");


    public JobService(JobRepository dao) {
        super(dao);
        this.dao = dao; //TODO: precisa mesmo?
    }

    public Job findByNome(@NotBlank String nome) {
        return dao.findByNome(nome);
    }

    public List<Job> persistAll(List<Job> jobs) {
        return jobs.stream()
            .map(this::persist)
            .collect(Collectors.toList());
    }

    public Job persist(@NotNull Job job) {
        log.info("Persistindo Job no banco:");
        log.info(job.toString());
        return dao.save(job);
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

    public List<JobDTO> mapearPlanilhaParaListaDto(
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
//            .peek(Job -> Job.setNomeArquivo(arquivoNome))
//            .peek(Job -> Job.setNomePlanilha(planilhaNome))
            .collect(Collectors.toList());
    }

    // TODO: javadoc
    private JobDTO refinarCampos(
        @NotNull JobPOJO jobPojo, @NotBlank String planilhaNome, @NotBlank String arquivoNome) {
        //---------------------------------------------------------------------------------------------
        val registro = "Job [" +jobPojo.getId()+ "] " +jobPojo.getJob();
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
        log.info("{}: Gerando JobDTO.", registro);
        val JobDto = new ModelMapper().map(jobPojo, JobDTO.class);
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
        JobDto.setNomeArquivo(arquivoNome);
        JobDto.setNomePlanilha(planilhaNome);
        try {
            log.info("{}: Tentando converter 'Data de Atualização' da planilha para o DTO.", registro);
            val data = refinarTexto(jobPojo.getDataAtualizacao());
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
    public EvidenciaPOJO executarPilha(EvidenciaPOJO evidenciaPOJO) {
        val Job = evidenciaPOJO.getRegistro();
        try {
            log.info("Preparando diretório para evidências desse Job.");
            val jobNome = Job.getJob().split("\\.")[0];
            val path = Arquivos.criarDiretorio(DIR_RECURSOS + jobNome).toPath();

            log.info("Tentando acessar ambiente remoto.");
            sftp = ConectorSftp.conectar("10.129.164.206", 22, "rcvry", "Ppw@1022");
            //TODO: obter e tratar corretamente o IP, PORTA, USUÁRIO E SENHA

            log.info("Obtendo log mais recente pré-execução.");
            val logAntes = downloadMaisRecente(Job, path);

            log.info("Executa comando do Job.");
            if(sftp.comando(evidenciaPOJO.comandoShell()).isEmpty())
                throw new RuntimeException("Comando não executado com sucesso.");

            log.info("Obtendo log mais recente pós-execução");
            val logDepois = downloadMaisRecente(Job, path);

            log.info("Comparando logs para anexar como evidência.");
            val logEvidencia = getArquivoMaisRecente(logAntes, logDepois);
            if(logEvidencia.isEmpty())
                throw new RuntimeException("Nenhum arquivo de log disponível para esse job.");

            log.info("Evidência de log coletada: ");
            printArquivo(logEvidencia.get());
            evidenciaPOJO.addEvidencias(logEvidencia.get());
            evidenciaPOJO.setSucesso(true);
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
            // 8. Se o comando SQL informado com declarações não permitidas (DELETE, DTOP, etc)
            log.error("Erro durante execução do job '{}': {}", Job.getJob(), e.getMessage());
        }
        return evidenciaPOJO;
    }

    //TODO: Javadoc
    @SafeVarargs
    private Optional<File> getArquivoMaisRecente(Optional<File>...arquivo) {
        return Stream.of(arquivo)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .max(Comparator.comparing(File::lastModified));
    }

    //TODO: Javadoc
    private Optional<File> downloadMaisRecente(JobDTO jobDTO, Path path) {
        val logDirRemoto = jobDTO.pathLog().toArray(new String[0]);
        log.info(Arrays.toString(logDirRemoto));

        //Realizando cada download para cada log informado pelo Job
        val sucessos = sftp.downloadMaisRecente(path, logDirRemoto);
        if(sucessos > 0)
            log.info("Total de downloads: " + sucessos);
        else {
            log.warn("Nenhum download realizado com sucesso.");
            return Optional.empty();
        }
        //Obtendo somente o arquivo mais recente
        final File arquivoMaisRecente = Arquivos.loadArquivos(path)
            .stream()
            .max(Comparator.comparing(File::lastModified))
            .orElse(null);
        if(arquivoMaisRecente != null) {
            printArquivo(arquivoMaisRecente);
            return Optional.of(arquivoMaisRecente);
        }
        log.warn("Nenhum arquivo de log disponível agora para esse job.");
        return Optional.empty();
    }

    //TODO: Javadoc
    private void printArquivo(@NonNull File arquivo) {
        if(!arquivo.exists()) return;
        val data = new Date(arquivo.lastModified());
        val peso = (double) arquivo.length() / 1000;
        log.info("\t > [{}] {} ({} Kbs)", data, arquivo.getName(), peso);
    }

}
