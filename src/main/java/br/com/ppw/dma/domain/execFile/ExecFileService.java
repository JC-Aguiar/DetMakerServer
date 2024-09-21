package br.com.ppw.dma.domain.execFile;

import br.com.ppw.dma.domain.master.MasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExecFileService extends MasterService<Long, ExecFile, ExecFileService> {

    private final ExecFileRepository dao;


    @Autowired
    public ExecFileService(ExecFileRepository dao) {
        super(dao);
        this.dao = dao;
    }

}
