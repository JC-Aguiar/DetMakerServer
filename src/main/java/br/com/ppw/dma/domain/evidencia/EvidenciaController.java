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
import java.util.HashSet;
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
    public ResponseEntity<String> salvarRevisao(
        @RequestBody List<EvidenciaRevisadaDTO> dtos)
    {
        val evidenciasId = dtos
            .stream()
            .map(EvidenciaRevisadaDTO::getEvidenciaId)
            .toList();
        log.info("Atualizando campos de revisão para as Evidências ID {}", evidenciasId);
        var relatorios = relatorioService.findAllByEvidenciaId(evidenciasId);
        if(relatorios.size() > 1) {
            var mensagemErro = "As Evidências apontam para Relatórios diferentes.";
            log.warn(mensagemErro);
            return new ResponseEntity<>(mensagemErro, HttpStatus.BAD_REQUEST);
        }
        //TODO: mover para dentro do Service
        var mensagemErro = "Tipo de resultado '%s' inválido. Os únicos valores permitidos são: "
            +  Arrays.stream(TipoEvidenciaStatus.values())
                .map(TipoEvidenciaStatus::getStatus)
                .collect(Collectors.joining(", "));
        var evidencias = evidenciaService.findById(evidenciasId);
        val erros = new HashSet<String>();

        dtos.forEach(dto -> {
            var id = dto.evidenciaId;
            try {
                val evidencia = evidencias
                    .stream()
                    .filter(ev -> Objects.equals(ev.getId(), dto.getEvidenciaId()))
                    .findFirst()
                    .orElse(null);
                if(evidencia == null) return;
                if(evidencia.getRevisor() != null && !evidencia.getRevisor().isBlank()) {
                    erros.add("Evidência ID " + id + " já revisada.");
                    return;
                }
                evidencia.setRevisor(dto.getResivor());
                evidencia.setDataRevisao(dto.getDataRevisao());
                evidencia.setComentario(dto.getComentario());
                TipoEvidenciaStatus
                    .identificar(dto.getResultado())
                    .ifPresentOrElse(
                        evidencia::setStatus,
                        () -> erros.add("Evidência ID " + id + " com resultado inválido: " + dto.getResultado())
                    );
            }
            catch(Exception e) {
                log.warn(e.getMessage());
                erros.add("Evidência ID " + id + " com erro: " + e.getMessage());
            }
        });
        if(!erros.isEmpty()) {
            erros.forEach(log::warn);
            return new ResponseEntity<>(String.join("\n", erros), HttpStatus.BAD_REQUEST);
        }
        evidenciaService.save(evidencias);
        log.info("Evidências atualizadas com sucesso.");
        return ResponseEntity.ok("Evidências atualizadas com sucesso.");
    }

}
