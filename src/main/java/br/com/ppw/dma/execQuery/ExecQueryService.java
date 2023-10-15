package br.com.ppw.dma.execQuery;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ExecQueryService {

    @Autowired
    private final ExecQueryRepository dao;

    public ExecQueryService(ExecQueryRepository dao) {
        this.dao = dao;
    }

    @Transactional
    public ExecQuery persist(@NotNull ExecQuery execQuery) {
        log.info("Persistindo ExecFile no banco:");
        log.info(execQuery.toString());

        execQuery = dao.save(execQuery);
        log.info("ExecQuery ID {} gravado com sucesso.", execQuery.getId());
        return execQuery;
    }

}
