package br.com.ppw.dma.domain.cliente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
        val record = dao.findById(id).orElseThrow();
        log.info("Registro encontrado no banco para ID {}:", id);
        log.info(record.toString());
        return record;
    }

    // A method that returns an Cliente by id.
    public Cliente findByClienteId(Long id) {
        var record = Optional.ofNullable(dao.findByAmbienteId(id)).orElseThrow();
        log.info("Cliente encontrado para Cliente ID {}:", id);
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
        log.info("Persistindo Cliente no banco:");
        log.info(cliente.toString());

        cliente = dao.save(cliente);
        log.info("Cliente ID {} gravado com sucesso.", cliente.getId());
        return cliente;
    }

    public List<Cliente> getByName(@NotNull String nome) {
        log.info("Consultando pela Cliente '{}'.", nome);
        val clientes = dao.findAllByNomeContaining(nome);
        if(!clientes.isEmpty())
            log.info("Total de Clientes encontrados: [{}]", clientes.size());
        else
            log.info("Cliente '{}' não encontrado.", nome);
        return clientes;
    }

    public boolean checkByName(@NotNull String nome) {
        log.info("Validando se a Cliente '{}' existe no banco.", nome);
        val resutlado = dao.existsByNome(nome);
        log.info("Resultado: {}.", resutlado);
        return resutlado;
    }

}
