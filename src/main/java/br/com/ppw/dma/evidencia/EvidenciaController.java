package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.job.ComandoSql;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("evidencia")
@Slf4j
public class EvidenciaController {

    private final EvidenciaService evidenciaService;
    public static final String PLANILHA_NOME = "DIÁRIA";

    public EvidenciaController(@Autowired EvidenciaService EvidenciaService) {
        this.evidenciaService = EvidenciaService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    //TODO: javadoc
    @PostMapping(value = "database")
    @Transactional
    public ResponseEntity<?> executarPilha(@RequestBody List<ComandoSql> comandosSql) {
//    public ResponseEntity<?> executarPilha(@RequestBody List<EvidenciaPostDTO> evidenciasDto) {
//        comandosSql.forEach(evidencia -> {
//            val result = evidenciaService.extractTable(evidencia.getQueries());
//            evidencia.addTabelasPreJob(result);
//            }
//        );
        final List<ExtrcaoBanco> result = evidenciaService.extractTable(comandosSql);
        return ResponseEntity.ok(result);
    }

}
