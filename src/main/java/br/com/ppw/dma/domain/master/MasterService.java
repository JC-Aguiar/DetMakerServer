package br.com.ppw.dma.domain.master;

import br.com.ppw.dma.exception.DuplicatedRecordException;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public abstract class MasterService<ID, ENTITY extends MasterEntity<ID>, THIS extends MasterService> {

    private JpaRepository<ENTITY, ID> dao;
    private final Type entityClass;

    // A constructor that injects the `dao` object.
    @Autowired
    public MasterService(JpaRepository dao) {
        this.dao = dao;
        final Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        this.entityClass = types[1];
    }

    /**
     * The proxy is a static method that returns a singleton instance of the service
     *
     * @return The proxy object.
     */
    protected final THIS proxy() {
        return (THIS) AopContext.currentProxy();
    }

    // A method that validates the page.
    public Page<ENTITY> pageCheck(@NonNull Page<ENTITY> page) {
        page.stream().map(Objects::nonNull).findFirst().orElseThrow();
        return page;
    }

    public List<ENTITY> save(@NonNull List<ENTITY> entities) {
        return dao.saveAll(entities);
    }

    public ENTITY save(@NonNull ENTITY entity) throws DuplicatedRecordException {
        log.info("Salvando {}.", entityClass.getTypeName());
        log.info(String.valueOf(entity));
        try {
            entity = dao.save(entity);
            log.info("ID gerado: [{}]", entity.getId());
            return entity;
        }
        catch(ConstraintViolationException e) {
            throw new DuplicatedRecordException();
        }
    }

    //TODO: melhorar
    // A method that returns an entity by id.
    public ENTITY findById(@NonNull ID id) {
        return dao.findById(id).orElseThrow();
    }

    // A method that returns an entity by id.
    public List<ENTITY> findById(@NonNull Collection<ID> ids) {
        return dao.findAllById(ids);
    }

    // A proxy method that calls `pageCheck` method.
    public Page<ENTITY> findAll(@NonNull Pageable pageable) {
        return proxy().pageCheck(dao.findAll(pageable));
    }

    @Profile("dev")
    public void deleteAll() {
        log.info("Deletando todas as entidades {}.", entityClass.getTypeName());
        dao.deleteAll();
    }

    @Profile("dev")
    public void delete(@NonNull ENTITY entity) {
        log.info("Deletando entidade {} ID {}.", entityClass.getTypeName(), entity.getId());
        dao.delete(entity);
    }

    @Profile("dev")
    public void delete(@NonNull Collection<? extends ENTITY> entities) {
        log.info("Total de entidades a serem deletadas: {}.", entities.size());
        entities.forEach(entity -> log.info(String.valueOf(entity)));
        dao.deleteAll(entities);
    }
}