package br.com.jcaguiar.cinephiles.master;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class MasterController<ID, MODEL, POST, GET> {

    protected final MasterService<ID, MODEL, POST, GET> service;

    MasterController(MasterService<ID, MODEL, POST, GET> service)
    {
        this.service = service;
    }

    @GetMapping(name = "{id}")
    protected ResponseEntity<?> getOne(@RequestParam(name = "id") String  id)
    {
        return null;
    }

}
