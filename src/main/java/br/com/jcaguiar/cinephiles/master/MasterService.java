package br.com.jcaguiar.cinephiles.master;

import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;
import java.util.Optional;

@Service
public abstract class MasterService<ID, ENTITY> {

    @Autowired
    private final JpaRepository<ENTITY, ID> dao;

    public MasterService(JpaRepository<ENTITY, ID> dao)
    {
        this.dao = dao;
    }

    private static final MasterService PROXY()
    {
        return (MasterService) AopContext.currentProxy();
    }

    @ConsoleLog
    public Page<ENTITY> pageCheck(@NotNull Page<ENTITY> page)
    {
        page.stream().map(Objects::nonNull).findFirst().orElseThrow();
        return page;
    }

    @ConsoleLog
    public ENTITY findById(@Positive @NotNull ID id)
    {
        return Optional.ofNullable(dao.getById(id)).orElseThrow();
    }

    @ConsoleLog
    public Page<?> findAll(@NotNull Pageable pageable)
    {
        return PROXY().pageCheck(dao.findAll(pageable));
    }
}
