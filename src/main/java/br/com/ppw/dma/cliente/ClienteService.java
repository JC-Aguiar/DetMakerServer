package br.com.ppw.dma.cliente;

import br.com.ppw.dma.job.Job;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClienteService {

    @Autowired
    private final ClienteRepository dao;

    public ClienteService(ClienteRepository dao) {
        this.dao = dao;
    }

    /**
     * The proxy is a static method that returns a singleton instance of the service
     *
     * @return The proxy object.
     */
    protected final ClienteService proxy() {
        return (ClienteService) AopContext.currentProxy();
    }

    // A method that returns an Cliente by id.
    public Cliente findById(@Positive @NotNull Long id) {
        val record = Optional
                .ofNullable(dao.getById(id))
                .orElseThrow();
        log.info("Registro encontrado no banco para ID {}:", id);
        log.info(record.toString());
        return record;
    }

    // A proxy method that calls `pageCheck` method.
    public List<Cliente> findAll() {
        return dao.findAll();
    }

    public List<Cliente> findAllById(List<Long> ids) {
        return dao.findAllById(ids);
    }

    @Transactional
    public Cliente persist(@NotNull Cliente cliente) {
        log.info("Persistindo Ambiente no banco:");
        log.info(cliente.toString());

        cliente = dao.save(cliente);
        log.info("Ambiente ID {} gravado com sucesso.", cliente.getId());
        return cliente;
    }

    public Optional<Cliente> getByName(@NotNull String nome) {
        log.info("Consultando pela Ambiente '{}'.", nome);
        val pipeline = Optional.ofNullable(dao.findAllByNome(nome));
        if(pipeline.isPresent())
            log.info("Ambiente '{}' obtida com sucesso.", nome);
        else
            log.info("Ambiente '{}' não encontrada.", nome);
        return pipeline;
    }

    public boolean checkByName(@NotNull String nome) {
        log.info("Validando se a Ambiente '{}' existe no banco.", nome);
        val resutlado = dao.existsByNome(nome);
        log.info("Resultado: {}.", resutlado);
        return resutlado;
    }

}
