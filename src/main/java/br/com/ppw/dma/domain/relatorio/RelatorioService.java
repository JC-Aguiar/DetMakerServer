package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.master.MasterService;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.pipeline.Pipeline;
import jakarta.persistence.PersistenceException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class RelatorioService extends MasterService<Long, Relatorio, RelatorioService> {

    @Autowired
    private final RelatorioRepository dao;


    public RelatorioService(RelatorioRepository dao) {
        super(dao);
        this.dao = dao;
    }

    public List<Relatorio> findAllByAmbiente(@NonNull Long clienteId) {
        val result = dao.findAllByAmbienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public Page<Relatorio> findAllByAmbiente(@NonNull Long ambienteId, @NonNull Pageable pageConfig) {
        val result = dao.findAllByAmbienteId(ambienteId, pageConfig);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public List<Relatorio> findAllByEvidenciaId(@NonNull List<Long> evidenciasId) {
        log.info("Obtendo Relatórios com base nos IDs das Evidências: {}.", evidenciasId );
        val result = dao.findAllByEvidenciaId(evidenciasId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public List<Relatorio> findAllFromPipeline(@NonNull Pipeline pipeline) {
//        log.info("Obtendo Relatórios no banco relacionados a Ambiente '{}'. ", pipeline.getProps().getNome());
        log.info("Obtendo Relatórios no banco relacionados a Ambiente '{}'. ", pipeline.getNome());
        val relatorios = dao.findAllByPipelineNome(pipeline.getNome());
        log.info("Total de Relatórios identificados: {}.", relatorios.size());
        relatorios.forEach(r -> log.info(r.toString()));
        return relatorios;
    }

    public Relatorio salvarRelatorioRevisado(@NonNull RelatorioComplementoDTO dto) {
        log.info("Atualizando campos do Relatório ID {}", dto.getId());
        val relatorio = findById(dto.getId());
        if(relatorio.isCompleto())
            throw new RuntimeException("Relatório já preenchido com Projeto e Atividade");

        relatorio.setIdProjeto(dto.getIdProjeto());
        relatorio.setNomeProjeto(dto.getNomeProjeto());
        relatorio.setNomeAtividade(dto.getNomeAtividade());
        Optional.ofNullable(dto.getConsideracoes()).ifPresentOrElse(
            relatorio::setConsideracoes,
            () -> relatorio.setConsideracoes("-")
        );
        return save(relatorio);
    }

    public Relatorio findMostRecentFromPipeline(@NonNull Pipeline pipeline) {
        val relatorios = findAllFromPipeline(pipeline);
        log.info("identificando Relatório mais recente");
        val relatorioMaisRecente = relatorios.stream()
            .min(Comparator.comparing(Relatorio::getData))
            .orElseThrow();
        log.info("Relatório identificado:");
        log.info(relatorioMaisRecente.toString());
        return relatorioMaisRecente;
    }

//    @Transactional(noRollbackFor = Throwable.class)
//    public Relatorio buildAndPersist(@NotNull PipelineResult pipelineResult) {
//        log.info("Convertendo Relatório DTO em Entidade.");
//        return persist(new Relatorio(pipelineResult));
//    }

//    @Transactional
//    public Relatorio persist(@NotNull Relatorio relatorio) {
//        log.info("Persistindo Relatório no banco:");
//        log.info(relatorio.toString());
//
//        relatorio = dao.save(relatorio);
//        log.info("Relatório ID {} gravado com sucesso.", relatorio.getId());
//        return relatorio;
//    }

    public Page<Relatorio> findAllByExample(Example<Relatorio> exemplo, Pageable pageConfig) {
        try {
            return dao.findAll(exemplo, pageConfig);
        }
        catch(PersistenceException e) {
            throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
        }
    }

}
