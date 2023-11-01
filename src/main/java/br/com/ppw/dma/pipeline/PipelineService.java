package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.master.MasterService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PipelineService extends MasterService<Long, Pipeline, PipelineService> {

    @Autowired
    private final PipelineRepository dao;

    public PipelineService(PipelineRepository dao) {
        super(dao);
        this.dao = dao;
    }

    @Transactional
    public Pipeline persist(@NotNull Pipeline pipeline) {
        log.info("Persistindo Pipeline no banco:");
        log.info(pipeline.toString());

        pipeline = dao.save(pipeline);
        log.info("Pipeline ID {} gravado com sucesso.", pipeline.getId());
        return pipeline;
    }

    public Pipeline getPipelineByName(@NotNull String nome) {
        log.info("Consultando pela Pipeline '{}'.", nome);
        val pipeline = dao.findAllByNome(nome);
        log.info("Pipeline obtida com sucesso.");
        return pipeline;
    }

}
