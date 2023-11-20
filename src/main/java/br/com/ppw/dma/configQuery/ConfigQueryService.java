package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.evidencia.EvidenciaRepository;
import br.com.ppw.dma.master.MasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ConfigQueryService extends MasterService<Long, ConfigQuery, ConfigQueryService> {

    @Autowired
    private final ConfigQueryRepository dao;


    public ConfigQueryService(ConfigQueryRepository dao) {
        super(dao);
        this.dao = dao;
    }

    public List<ConfigQuery> findAllByJobId(Long id) {
        return dao.findAllByJobId(id);
    }


}
