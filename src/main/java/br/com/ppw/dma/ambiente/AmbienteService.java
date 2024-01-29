package br.com.ppw.dma.ambiente;

import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class AmbienteService {

    @Autowired private AmbienteRepository dao;

    /**
     * The proxy is a static method that returns a singleton instance of the service
     *
     * @return The proxy object.
     */
    protected final AmbienteService proxy() {
        return (AmbienteService) AopContext.currentProxy();
    }

    public Optional<Ambiente> addOne(@NotNull Ambiente entity) throws DuplicatedRecordException {
        try {
            return Optional.of(dao.save(entity));
        }
        catch(ConstraintViolationException e) {
            throw new DuplicatedRecordException();
        }
    }

    // A method that returns an entity by id.
    public Ambiente findById(@Positive @NotNull Long id) {
        val record = Optional
            .ofNullable(dao.getById(id))
            .orElseThrow();
        log.info("Registro encontrado no banco para ID {}:", id);
        log.info(record.toString());
        return record;
    }

    // A proxy method that calls `pageCheck` method.
    public List<Ambiente> findAll() {
        return dao.findAll();
    }

    // A method that validates the page.
    public Page<Ambiente> pageCheck(@NotNull Page<Ambiente> page) {
        page.stream().map(Objects::nonNull).findFirst().orElseThrow();
        return page;
    }

    // A proxy method that calls `pageCheck` method.
    public Page<Ambiente> findAll(@NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findAll(pageable));
    }

    public List<Ambiente> findAllById(List<Long> ids) {
        return dao.findAllById(ids);
    }

    public List<Ambiente> findAllFromCliente(@NonNull Cliente cliente) {
        log.info("Obtendo Ambientes no banco do Cliente '{}'. ", cliente.getNome());
        val ambientes = dao.findAllByCliente(cliente);
        log.info("Total de Ambientes identificados: {}.", ambientes.size());
        ambientes.forEach(amb -> log.info(" - '{}'", amb.getNome()));
        return ambientes;
    }

    @Transactional
    public Ambiente persist(@NotNull Ambiente ambiente) {
        log.info("Persistindo Ambiente no banco:");
        log.info(ambiente.toString());

        ambiente = dao.save(ambiente);
        log.info("Ambiente ID {} gravado com sucesso.", ambiente.getId());
        return ambiente;
    }

    public Optional<Ambiente> getByName(@NotNull String nome) {
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
