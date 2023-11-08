package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public Optional<Pipeline> getPipelineByName(@NotNull String nome) {
        log.info("Consultando pela Pipeline '{}'.", nome);
        val pipeline = Optional.ofNullable(dao.findAllByNome(nome));
        if(pipeline.isPresent())
            log.info("Pipeline '{}' obtida com sucesso.", nome);
        else
            log.info("Pipeline '{}' não encontrada.", nome);
        return pipeline;
    }

    public boolean checkPipelineByName(@NotNull String nome) {
        log.info("Validando se a Pipeline '{}' existe no banco.", nome);
        val resutlado = dao.existsByNome(nome);
        log.info("Resultado: {}.", resutlado);
        return resutlado;
    }

    public Pipeline parsePipelineNovaExecDTO(
            @NotNull PipelineNovaExecDTO novaPipeline,
            @NotNull List<Job> jobs) {
        //------------------------------------------------------------------
        log.info("Convertendo Pipeline: de PipelineNovaExecDTO em Entidade.");
        val pipeline = new Pipeline();
        pipeline.setNome(novaPipeline.getPipeline().getNome());
        pipeline.setDescricao(novaPipeline.getPipeline().getDescricao());
        pipeline.setJobs(jobs);
        return pipeline;
    }

}
