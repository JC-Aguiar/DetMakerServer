package br.com.ppw.dma.master;

import br.com.ppw.dma.exception.DuplicatedRecordException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public abstract class MasterService<ID, ENTITY, THIS extends  MasterService> {

    private JpaRepository<ENTITY, ID> dao;


    // A constructor that injects the `dao` object.
    public MasterService(@Autowired JpaRepository dao) {
        this.dao = dao;
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
    public Page<ENTITY> pageCheck(@NotNull Page<ENTITY> page) {
        page.stream().map(Objects::nonNull).findFirst().orElseThrow();
        return page;
    }

    public Optional<ENTITY> addOne(@NotNull ENTITY entity) throws DuplicatedRecordException {
        try {
            return Optional.of(dao.save(entity));
        }
        catch (ConstraintViolationException e) {
            throw new DuplicatedRecordException();
        }
    }

    // A method that returns an entity by id.
    public ENTITY findById(@Positive @NotNull ID id) {
        return Optional
            .ofNullable(dao.getById(id))
            .orElseThrow();
    }

    // A proxy method that calls `pageCheck` method.
    public Page<ENTITY> findAll(@NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findAll(pageable));
    }

    public List<ENTITY> findAllById(List<ID> ids) {
        return dao.findAllById(ids);
    }

    @Profile("dev")
    public void deleteAll() {
        dao.deleteAll();
    }

    @Profile("dev")
    public void delete(@NotNull ENTITY entity) {
        dao.delete(entity);
    }
}