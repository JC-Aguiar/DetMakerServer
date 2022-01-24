package br.com.jcaguiar.cinephiles.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class MasterService<ID, MODEL, POST, GET> {

    @Autowired
    protected JpaRepository<MODEL, ID> dao;

}
