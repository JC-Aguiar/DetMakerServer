package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.master.MasterSummary;
import br.com.ppw.dma.domain.relatorio.RelatorioHistoricoDTO;
import br.com.ppw.dma.domain.relatorio.RelatorioService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("evidencia")
@Slf4j
public class EvidenciaController extends MasterController<Long, Evidencia, EvidenciaController> {

    private final EvidenciaService evidenciaService;
    private final RelatorioService relatorioService;


    @Autowired
    public EvidenciaController(EvidenciaService evidenciaService, RelatorioService relatorioService) {
        super(evidenciaService);
        this.evidenciaService = evidenciaService;
        this.relatorioService = relatorioService;
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
        @RequestBody List<EvidenciaRevisadaDTO> dtos) {
        //------------------------------------------------------------
        val evidenciasId = dtos
            .stream()
            .map(EvidenciaRevisadaDTO::getEvidenciaId)
            .toList();
        log.info("Atualizando campos de revisão para as Evidências ID {}", evidenciasId);
        val resumo = new MasterSummary<Long>();
        var relatorios = relatorioService.findAllByEvidenciaId(evidenciasId);
        if(relatorios.size() > 1) {
            evidenciasId.forEach(
                id -> resumo.fail(id, "As Evidências apontam para Relatórios diferentes."));
            return new ResponseEntity<>(resumo, HttpStatus.BAD_REQUEST);
        }
        //TODO: mover para dentro do Service
        var mensagemErro = "Tipo de resultado '%s' inválido. Os únicos valores permitidos são: "
            +  Arrays.stream(TipoEvidenciaStatus.values())
                .map(TipoEvidenciaStatus::getStatus)
                .collect(Collectors.joining(", "));
        var evidencias = evidenciaService.findById(evidenciasId);
        dtos.forEach(dtoRevisao -> {
            try {
                val evidencia = evidencias
                    .stream()
                    .filter(ev -> Objects.equals(ev.getId(), dtoRevisao.getEvidenciaId()))
                    .findFirst()
                    .orElse(null);
                if(evidencia == null) return;
                if(evidencia.getRevisor() != null && !evidencia.getRevisor().isBlank()) {
                    resumo.fail(dtoRevisao.evidenciaId, "Evidência já revisada.");
                    return;
                }
                evidencia.setRevisor(dtoRevisao.getResivor());
                evidencia.setDataRevisao(dtoRevisao.getDataRevisao());
                evidencia.setComentario(dtoRevisao.getComentario());
                TipoEvidenciaStatus
                    .identificar(dtoRevisao.getResultado())
                    .ifPresentOrElse(
                        evidencia::setStatus,
                        () -> resumo.fail(
                            dtoRevisao.evidenciaId,
                            String.format(mensagemErro, dtoRevisao.getResultado())
                    ));
                evidenciaService.save(evidencia);
                resumo.save(dtoRevisao.evidenciaId);
            }
            catch(Exception e) {
                log.warn(e.getMessage());
                resumo.fail(dtoRevisao.evidenciaId, e.getMessage());
            }
        });
        log.info("Total de Evidências atualizadas com os campos de revisão: {}.", resumo.totalSize());
        log.info("Sucessos: {}.", resumo.getSaved().size());
        log.info("Falhas: {}.", resumo.getFailed().size());

        if(resumo.getFailed().isEmpty()) return ResponseEntity.ok(resumo);
        return new ResponseEntity<>(resumo, HttpStatus.NO_CONTENT);
    }

}
