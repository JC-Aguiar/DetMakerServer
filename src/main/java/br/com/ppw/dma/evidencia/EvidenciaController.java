package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.exception.TipoEvidenciaResultadoException;
import br.com.ppw.dma.job.JobExecutePOJO;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
import br.com.ppw.dma.master.MasterSummaryDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("evidencia")
@Slf4j
public class EvidenciaController extends MasterController
    <Long, Evidencia, MasterRequestDTO, MasterResponseDTO, EvidenciaController> {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(@Autowired EvidenciaService evidenciaService) {
        super(evidenciaService);
        this.evidenciaService = evidenciaService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Override
    public ResponseEntity<EvidenciaInfoDTO> getOne(Long aLong) throws NoSuchMethodException {
        val evidencia = evidenciaService.findById(aLong);
        val dto = new EvidenciaInfoDTO(evidencia);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<Page<EvidenciaInfoDTO>> getAll(int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final Page<EvidenciaInfoDTO> dtos = evidenciaService.findAll(pageConfig)
            .map(EvidenciaInfoDTO::new);
        return ResponseEntity.ok(dtos);
    }

    //TODO: javadoc
    @Transactional
    @PostMapping(value = "save/review")
    public ResponseEntity<MasterSummaryDTO<Long>> salvarRevisao(
        @RequestBody List<EvidenciaRevisadaDTO> evidencias) {
        //------------------------------------------------------------
        val evidenciasId = evidencias
            .stream()
            .map(EvidenciaRevisadaDTO::getEvidenciaId)
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
        log.info("Atualizando campos de revisão para as Evidências ID {}", evidenciasId);

        val resumo = new MasterSummaryDTO<Long>();
        evidencias.forEach(evRevisada -> {
            try {
                val evidencia = evidenciaService.findById(evRevisada.evidenciaId);
                evidencia.setResivor(evRevisada.getResivor());
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
                resumo.fail(evRevisada.evidenciaId);
            }
        });
        log.info("Total de Evidências atualizadas com os campos de revisão: {}.", resumo.totalSize());
        log.info("Sucessos: {}.", resumo.getSaved().size());
        log.info("Falhas: {}.", resumo.getFailed().size());

        if(resumo.getFailed().isEmpty()) return ResponseEntity.ok(resumo);
        return new ResponseEntity<>(resumo, HttpStatus.NO_CONTENT);
    }

    //TODO: javadoc
    public List<Evidencia> gerarEvidencias(@NonNull List<JobExecutePOJO> jobsPojo) {
        log.info("Iniciando rotina para gerar Evidências.");
        val listaEvidencias = new ArrayList<Evidencia>();
        for(val jobPojo : jobsPojo) {
            try {
                val evidencia = evidenciaService.createEvidencia(jobPojo);
                listaEvidencias.add(evidencia);
            }
            catch(Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return listaEvidencias;
    }

}
