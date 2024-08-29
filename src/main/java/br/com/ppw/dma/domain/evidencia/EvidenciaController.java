package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.master.MasterSummary;
import br.com.ppw.dma.exception.TipoEvidenciaResultadoException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("evidencia")
@Slf4j
public class EvidenciaController extends MasterController<Long, Evidencia, EvidenciaController> {

    private final EvidenciaService evidenciaService;


    public EvidenciaController(@Autowired EvidenciaService evidenciaService) {
        super(evidenciaService);
        this.evidenciaService = evidenciaService;
    }

    @Override
    public ResponseEntity<?> parseOne(Evidencia entity) {
        val dto = new EvidenciaInfoDTO(entity);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<?> parseAll(Page<Evidencia> evidencias) {
        final Page<EvidenciaInfoDTO> dtos = evidencias.map(EvidenciaInfoDTO::new);
        return ResponseEntity.ok(dtos);
    }

    //TODO: javadoc
    @Transactional
    @PostMapping(value = "review")
    public ResponseEntity<MasterSummary<Long>> salvarRevisao(
        @RequestBody List<EvidenciaRevisadaDTO> evidencias) {
        //------------------------------------------------------------
        val evidenciasId = evidencias
            .stream()
            .map(EvidenciaRevisadaDTO::getEvidenciaId)
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
        log.info("Atualizando campos de revisão para as Evidências ID {}", evidenciasId);

        val resumo = new MasterSummary<Long>();
        evidencias.forEach(evRevisada -> {
            try {
                val evidencia = evidenciaService.findById(evRevisada.evidenciaId);
                evidencia.setRevisor(evRevisada.getResivor());
                evidencia.setDataRevisao(evRevisada.getDataRevisao());
                evidencia.setComentario(evRevisada.getComentario());
                evidencia.setResultado(
                    TipoEvidenciaResultado.identificar(evRevisada.getResultado())
                    .orElseThrow(() -> new TipoEvidenciaResultadoException(evRevisada.getResultado()))
                );
                evidenciaService.persist(evidencia);
                resumo.save(evRevisada.evidenciaId);
            }
            catch(TipoEvidenciaResultadoException e) {
                log.warn(e.getMessage());
                resumo.fail(evRevisada.evidenciaId, e.getMessage());
            }
        });
        log.info("Total de Evidências atualizadas com os campos de revisão: {}.", resumo.totalSize());
        log.info("Sucessos: {}.", resumo.getSaved().size());
        log.info("Falhas: {}.", resumo.getFailed().size());

        if(resumo.getFailed().isEmpty()) return ResponseEntity.ok(resumo);
        return new ResponseEntity<>(resumo, HttpStatus.NO_CONTENT);
    }

}
