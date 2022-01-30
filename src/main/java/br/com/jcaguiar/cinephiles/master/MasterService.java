package br.com.jcaguiar.cinephiles.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public abstract class MasterService<ID, MODEL, POST, GET> {

    protected final JpaRepository<MODEL, ID> dao;

    MasterService(JpaRepository<MODEL, ID> dao)
    {
        this.dao = dao;
    }

}
