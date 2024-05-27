package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.master.MasterService;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class PipelineService extends MasterService<PipelineProps, Pipeline, PipelineService> {

    @Autowired
    private final PipelineRepository dao;


    public PipelineService(PipelineRepository dao) {
        super(dao);
        this.dao = dao;
    }

    public List<Pipeline> findAllByCliente(@NonNull Long clienteId) {
        val result = dao.findAllByClienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    @Transactional
    public Pipeline persist(@NotNull Pipeline pipeline) {
        log.info("Persistindo Pipeline no banco:");
        log.info(pipeline.toString());

        pipeline = dao.save(pipeline);
        log.info("Pipeline ID {} gravado com sucesso.", pipeline.getId());
        return pipeline;
    }

    public Optional<Pipeline> getUniqueOne(@NotNull String nome, @NotNull Long clienteId) {
        log.info("Consultando Pipeline '{}', Cliente ID {}.", nome, clienteId);
        val pipeline = Optional.ofNullable(dao.findByNomeAndCliente(nome, clienteId));
        if(pipeline.isPresent())
            log.info("Pipeline '{}' obtida com sucesso.", nome);
        else
            log.info("Pipeline '{}' não encontrada.", nome);
        return pipeline;
    }

    public void checkDuplicated(@NotNull String nome, @NotNull Long clienteId)
    throws DuplicatedRecordException {
        log.debug("Iniciando validação contra duplicidade, conversão e persistência.");
        if(getUniqueOne(nome, clienteId).isPresent()) throw new DuplicatedRecordException();
    }

}
