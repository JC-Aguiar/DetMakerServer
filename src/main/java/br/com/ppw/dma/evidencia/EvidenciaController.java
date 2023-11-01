package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.job.JobExecutePOJO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.ResultadoSql;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("evidencia")
@Slf4j
public class EvidenciaController extends MasterController
    <Long, Evidencia, MasterRequestDTO, MasterResponseDTO, EvidenciaController> {

    private final EvidenciaService evidenciaService;
    public static final String PLANILHA_NOME = "DIÁRIA";

    public EvidenciaController(@Autowired EvidenciaService evidenciaService) {
        super(evidenciaService);
        this.evidenciaService = evidenciaService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    //TODO: javadoc
    @PostMapping(value = "database")
    @Transactional
    public ResponseEntity<?> executarPilha(@RequestBody List<ComandoSql> comandosSql) {
//    public ResponseEntity<?> executarPilha(@RequestBody List<JobExecuteDTO> evidenciasDto) {
//        comandosSql.forEach(evidencia -> {
//            val result = evidenciaService.extractTable(evidencia.getQueries());
//            evidencia.addTabelasPreJob(result);
//            }
//        );
        final List<ResultadoSql> result = evidenciaService.extractTablePreJob(comandosSql);
        return ResponseEntity.ok(result);
    }

    public List<Evidencia> gerarEvidencias(@NonNull List<JobExecutePOJO> jobsPojo) {
        val listaEvidencias = new ArrayList<Evidencia>();
        for(val jobPojo : jobsPojo) {
            try {
                val ordem = jobPojo.getOrdem();
                val envidencia = evidenciaService.createEvidencia(jobPojo);
                listaEvidencias.add(envidencia);
            }
            catch(Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listaEvidencias;
    }

    public Evidencia createEvidencia(JobExecutePOJO jobPojo) {
        return evidenciaService.createEvidencia(jobPojo);
    }

    public EvidenciaInfoDTO parseToResponseDto(@NonNull Evidencia evidencia, @NonNull Integer ordem) {
        return evidenciaService.parseToResponseDto(evidencia, ordem);
    }
}
