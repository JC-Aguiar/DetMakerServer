package br.com.ppw.dma.domain.execQuery;

import br.com.ppw.dma.domain.master.MasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExecQueryService extends MasterService<Long, ExecQuery, ExecQueryService> {

    private final ExecQueryRepository dao;


    @Autowired
    public ExecQueryService(ExecQueryRepository dao) {
        super(dao);
        this.dao = dao;
    }

}
