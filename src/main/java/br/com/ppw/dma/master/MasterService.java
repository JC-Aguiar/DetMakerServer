package br.com.ppw.dma.master;

import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.util.ConsoleLog;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;
import java.util.Optional;

@Service
public abstract class MasterService<
    ID, ENTITY, THIS extends  MasterService> {

    @Autowired
    private final JpaRepository<ENTITY, ID> dao;

    // A constructor that injects the `dao` object.
    public MasterService(JpaRepository dao) {
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
    @ConsoleLog
    public Page<ENTITY> pageCheck(@NotNull Page<ENTITY> page) {
        page.stream().map(Objects::nonNull).findFirst().orElseThrow();
        return page;
    }

    public Optional<ENTITY> addOne(@NotNull ENTITY entity) throws DuplicatedRecordException {
        try {
            return Optional.of(dao.save(entity));
        } catch (ConstraintViolationException e) {
            throw new DuplicatedRecordException();
        }
    }

    // A method that returns an entity by id.
    @ConsoleLog
    public ENTITY findById(@Positive @NotNull ID id) throws NoSuchMethodException {
        return Optional.ofNullable(dao.getById(id))
           .orElseThrow();
    }

    // A proxy method that calls `pageCheck` method.
    @ConsoleLog
    public Page<?> findAll(@NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findAll(pageable));
    }

    @Profile("teste")
    @ConsoleLog
    public Optional<ENTITY> deleteAll() {
        dao.deleteAll();
        return Optional.empty();
    }
}
