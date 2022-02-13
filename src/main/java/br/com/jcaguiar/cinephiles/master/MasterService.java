package br.com.jcaguiar.cinephiles.master;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

@Service
public abstract class MasterService<ID, ENTITY> {

    protected final JpaRepository<ENTITY, ID> dao;

    public MasterService(JpaRepository<ENTITY, ID> dao)
    {
        this.dao = dao;
    }

    private Page<?> pageCheck(@NotNull Page<?> page) {
        page.stream().map(Objects::nonNull).findFirst().orElseThrow();
        return page;
    }

    public ENTITY findById(@NotNull ID id) {
        return Optional.ofNullable(dao.getById(id)).orElseThrow();
    }

    public Page<?> findAll(@NotNull Pageable pageable) {
        return pageCheck(dao.findAll(pageable));
    }
}
