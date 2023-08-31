package br.com.ppw.dma.job;

import br.com.ppw.dma.batch.Arquivos;
import br.com.ppw.dma.batch.ExcelXLSX;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.net.ConectorSftp;
import com.google.gson.Gson;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.DetMakerApplication.DIR_RECURSOS;
import static br.com.ppw.dma.DetMakerApplication.RELOGIO;
import static br.com.ppw.dma.util.FormatString.*;

@Service
@Slf4j
public class AgendaService extends MasterService<AgendaID, Agenda, AgendaService> {

    @Autowired
    private Gson gson;

    @Autowired
    private final AgendaRepository dao;

    private ConectorSftp sftp;

    public static final DateTimeFormatter CONVERSOR_DATA_SCHEDULE = DateTimeFormatter
        .ofPattern("dd/MM/yyyy");


    public AgendaService(AgendaRepository dao) {
        super(dao);
        this.dao = dao; //TODO: precisa mesmo?
    }

    public List<Agenda> persistAll(List<Agenda> agendas) {
        return agendas.stream()
            .map(this::persist)
            .collect(Collectors.toList());
    }

    public Agenda persist(Agenda agenda) {
        log.info("Persistindo agenda no banco:");
        agenda.setDataRegistro(OffsetDateTime.now(RELOGIO));
        agenda.setAutorRegistro("PENDENTE DE DESENVOLVER"); //TODO: alterar para colocar o nome do usuário
        agenda.setOrigemRegistro("DET-MAKER-API v1.0.Beta");
        log.info(agenda.toString());
        return dao.save(agenda);
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

    public List<AgendaDTO> mapearPlanilhaParaListaDto(
        @NotNull ExcelXLSX excel, @NotBlank String planilhaNome) {
        //--------------------------------------------------------
        val arquivoNome = excel.getNomeArquivo();
        log.info("Tentando ler registros da planilha '{}'.", planilhaNome);
        return excel.getPlanilhas()
            .stream()
            .filter(planilha -> planilha.getNome().equals(planilhaNome))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Planilha " + planilhaNome + " não encontrada."))
            .getAgendaPOJOS()
            .stream()
            .map(agenda -> refinarCampos(agenda, planilhaNome, arquivoNome))
//            .peek(agenda -> agenda.setNomeArquivo(arquivoNome))
//            .peek(agenda -> agenda.setNomePlanilha(planilhaNome))
            .collect(Collectors.toList());
    }

    // TODO: javadoc
    private AgendaDTO refinarCampos(
        @NotNull AgendaPOJO agendaPojo, @NotBlank String planilhaNome, @NotBlank String arquivoNome) {
        //---------------------------------------------------------------------------------------------
        val registro = "Job [" +agendaPojo.getId()+ "] " +agendaPojo.getJob();
        log.info("{}: Validando os campos possuem conteúdo de fato ou apenas indicadores vazios.", registro);
        val tabelas = valorVazio(agendaPojo.getTabelas().replace("RCVRY.", ""));
        val parametroNome = valorVazio(agendaPojo.getParametros());
        val parametroDescricao = valorVazio(agendaPojo.getDescricaoParametros());
        val diretorioEntrada = valorVazio(agendaPojo.getDiretorioEntrada());
        val mascaraEntrada = valorVazio(agendaPojo.getMascaraEntrada());
        val diretorioSaida = valorVazio(agendaPojo.getDiretorioSaida());
        val mascaraSaida = valorVazio(agendaPojo.getMascaraSaida());
        val diretorioLog = valorVazio(agendaPojo.getDiretorioLog());
        val mascaraLog = valorVazio(agendaPojo.getMascaraLog());

        log.info("{}: Detectando se há separadores no texto para dividi-los em listas.", registro);
        val listaExecPosJob = dividirValores(agendaPojo.getExecutarAposJob());
        val listaPrograma = dividirValores(agendaPojo.getPrograma());
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
        log.info("{}: Gerando AgendaDTO.", registro);
        val agendaDto = new ModelMapper().map(agendaPojo, AgendaDTO.class);
        agendaDto.setDiretorioEntrada(diretorioEntrada);
        agendaDto.setDiretorioSaida(diretorioSaida);
        agendaDto.setDiretorioLog(diretorioLog);
        agendaDto.setExecutarAposJob(listaExecPosJob);
        agendaDto.setPrograma(listaPrograma);
        agendaDto.setTabelas(listaTabelas);
        agendaDto.setParametros(listaParametrosNome);
        agendaDto.setDescricaoParametros(listaParametrosDescricao);
        agendaDto.setMascaraEntrada(listaMascarasEntrada);
        agendaDto.setMascaraSaida(listaMascarasSaida);
        agendaDto.setMascaraLog(listaMascarasLog);
        agendaDto.setNomeArquivo(arquivoNome);
        agendaDto.setNomePlanilha(planilhaNome);
        try {
            log.info("{}: Tentando converter 'Data de Atualização' da planilha para o DTO.", registro);
            val data = refinarTexto(agendaPojo.getDataAtualizacao());
            agendaDto.setDataAtualizacao(LocalDate.parse(data, CONVERSOR_DATA_SCHEDULE));
            log.info("{}: Conversão realizada com sucesso.", registro);
        }
        catch(Exception e) {
            log.info("{}: Não foi possível converter a data: {}.", registro, e.getMessage());
        }
        log.info("{}: {}", registro, agendaDto);
        return agendaDto;
    }

    //TODO: javadoc
    public Evidencia executarPilha(Evidencia evidencia) {
        val agenda = evidencia.getRegistro();
        try {
            log.info("Preparando diretório para evidências desse Job.");
            val jobNome = agenda.getJob().split("\\.")[0];
            val path = Arquivos.criarDiretorio(DIR_RECURSOS + jobNome).toPath();

            log.info("Tentando acessar ambiente remoto.");
            sftp = ConectorSftp.conectar("10.129.164.206", 22, "rcvry", "Ppw@1022");
            //TODO: obter e tratar corretamente o IP, PORTA, USUÁRIO E SENHA

            log.info("Obtendo log mais recente pré-execução.");
            val logAntes = downloadMaisRecente(agenda, path);

            log.info("Executa comando do Job.");
            if(sftp.comando(evidencia.comandoShell()).isEmpty())
                throw new RuntimeException("Comando não executado com sucesso.");

            log.info("Obtendo log mais recente pós-execução");
            val logDepois = downloadMaisRecente(agenda, path);

            log.info("Comparando logs para anexar como evidência.");
            val logEvidencia = getArquivoMaisRecente(logAntes, logDepois);
            if(logEvidencia.isEmpty())
                throw new RuntimeException("Nenhum arquivo de log disponível para esse job.");

            log.info("Evidência de log coletada: ");
            printArquivo(logEvidencia.get());
            evidencia.addEvidencias(logEvidencia.get());
            evidencia.setSucesso(true);
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
            log.error("Erro durante execução do job '{}': {}", agenda.getJob(), e.getMessage());
        }
        return evidencia;
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
    private Optional<File> downloadMaisRecente(AgendaDTO agendaDTO, Path path) {
        val logDirRemoto = agendaDTO.pathLog().toArray(new String[0]);
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
