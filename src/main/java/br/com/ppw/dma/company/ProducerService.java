package br.com.ppw.dma.company;

import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.util.ConsoleLog;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
public class ProducerService extends MasterService<Integer, ProducerEntity, ProducerService> {

    private final ProducerRepository dao;

    public ProducerService(@NotNull ProducerRepository dao) {
        super(dao);
        this.dao = dao;
    }

    @ConsoleLog
    public ProducerEntity loadOrSave(@NotBlank String name) {
        return Optional.ofNullable(dao.findByName(name))
            .orElseGet(() -> newCompany(name));
    }

    @ConsoleLog
    private ProducerEntity newCompany(@NotBlank String name) {
        final ProducerEntity companyEntity = ProducerEntity
            .builder().name(name).build();
        return dao.saveAndFlush(companyEntity);
    }

}
