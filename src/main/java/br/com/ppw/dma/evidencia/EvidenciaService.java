package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execFile.ExecFileService;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.execQuery.ExecQueryService;
import br.com.ppw.dma.job.JobProcess;
import br.com.ppw.dma.master.MasterService;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Service
@Slf4j
public class EvidenciaService extends MasterService<Long, Evidencia, EvidenciaService> {

    @Autowired
    private final EvidenciaRepository evidenciaDao;

    @Autowired
    private final ExecFileService execFileService;

    @Autowired
    private final ExecQueryService execQueryService;

    @Autowired
    private Gson gson;

    public EvidenciaService(
        EvidenciaRepository evidenciaDao,
        ExecFileService execFileService,
        ExecQueryService execQueryService,
        Gson gson) {
        //---------------------------------------
        super(evidenciaDao);
        this.evidenciaDao = evidenciaDao;
        this.execFileService = execFileService;
        this.execQueryService = execQueryService;
        this.gson = gson;
    }

    @Transactional
    public Evidencia persist(@NotNull Evidencia evidencia) {
        log.info("Persistindo Evidencia no banco:");
        log.info(evidencia.toString());
        evidencia = evidenciaDao.save(evidencia);

        log.info("Evidência ID {} gravado com sucesso.", evidencia.getId());
        return evidencia;
    }

    //TODO: javadoc
    public List<Evidencia> gerarEvidencia(@NonNull List<JobProcess> jobProcesses) {
        log.info("Iniciando geração de Evidências para {} registro(s).", jobProcesses.size());
        val listaEvidencias = new ArrayList<Evidencia>();
        for(val jobPojo : jobProcesses) {
            try {
                val evidencia = gerarEvidencia(jobPojo);
                listaEvidencias.add(evidencia);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return listaEvidencias;
    }

    //TODO: javadoc
    @Transactional
    public Evidencia gerarEvidencia(@NonNull JobProcess process) {
        log.info("Gerando Evidência para o JobProcess:");
        log.info(process.toString());
        val logs = new ArrayList<ExecFile>();

        log.info("Criando novo registro da Evidência.");
        val evidencia = persist(new Evidencia(process));
        evidenciaDao.flush();
        log.info(evidencia.toString());

        if(process.possuiTabelas())
            log.info("Criando novos registros ExecQuery para cada resultado no banco (pré e pós Job).");
        val banco = new ArrayList<ExecQuery>();
        for(int i = 0; i < process.getTabelasPosJob().size(); i++) {
            val tabelaPre = process.getTabelasPreJob().get(i);
            val tabelaPos = process.getTabelasPosJob().get(i);
            if(!tabelaPre.getNome().equals(tabelaPos.getNome()))
                continue;
            val query = ExecQuery.montarEvidencia(evidencia, tabelaPre, tabelaPos);
            banco.add(execQueryService.persist(query));
        }

        if(!process.getCargas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das cargas usadas.");
        val cargas = process.getCargas()
            .stream()
            .map(carga -> ExecFile.montarEvidenciaCarga(evidencia, carga))
            .map(execFileService::persist)
            .toList();

        if(!process.getLogs().isEmpty() || !process.getTerminal().isEmpty())
            log.info("Criando novos registros ExecFile para cada um dos logs obtidos.");
        final String terminalConteudo = process.getTerminalFormatado();
        if(!terminalConteudo.isEmpty()) {
            val execFileTerminal = ExecFile.montarEvidenciaTerminal(evidencia, terminalConteudo);
            logs.add(execFileTerminal);
        }
        process.getLogs()
            .stream()
            .map(log -> ExecFile.montarEvidenciaLog(evidencia, log))
            .map(execFileService::persist)
            .forEach(logs::add);

        if(!process.getSaidas().isEmpty())
            log.info("Criando novos registros ExecFile para cada uma das saídas produzidas.");
        val saidas = process.getSaidas()
            .stream()
            .map(saida -> ExecFile.montarEvidenciaSaida(evidencia, saida))
            .map(execFileService::persist)
            .toList();

        log.info("Atualizando Evidência ID {} com os anexos (ExecFile e ExecQuery).", evidencia.getId());
        evidencia.setBanco(banco);
        evidencia.setCargas(cargas);
        evidencia.setLogs(logs);
        evidencia.setSaidas(saidas);

        if(evidencia.getErroFatal().isEmpty()) return evidencia;

        evidencia.setRevisor("Det-Maker");
        evidencia.setDataRevisao(OffsetDateTime.now(RELOGIO));
        evidencia.setResultado(TipoEvidenciaResultado.REPROVADO);
        evidencia.setComentario(
            "A aplicação não conseguiu executar o Job com sucesso e seu resultado foi definido automaticamente");
        return evidencia;
    }

//    public File parseBlobToFile(@NonNull Blob blob, @NotBlank String filePath){
//        try(InputStream inputStream = blob.getBinaryStream()) {
//            log.info("Lendo os dados do Blob como uma String.");
//            byte[] bytes = inputStream.readAllBytes();
//            val jsonString = new String(bytes, StandardCharsets.UTF_8);
//
//            log.info("Salvando o Json em um arquivo no diretório: '{}'.", filePath);
//            try(val outputStream = new FileOutputStream(filePath)) {
//                val gson = new Gson();
//                val json = gson.toJson(jsonString);
//                outputStream.write(json.getBytes(StandardCharsets.UTF_8));
//                log.info("Arquivo salvo com sucesso.");
//            }
//        }
//        catch(Exception e) {
//            log.warn("Falha ao tentar interpretar Blob: {}", e.getMessage());
//        }
//        return new File(filePath);
//    }


}
