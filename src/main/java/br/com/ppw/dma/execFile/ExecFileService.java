package br.com.ppw.dma.execFile;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execFile.ExecFileRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ExecFileService {

    @Autowired
    private final ExecFileRepository dao;

    public ExecFileService(ExecFileRepository dao) {
        this.dao = dao;
    }

    @Transactional
    public ExecFile persist(@NotNull ExecFile execFile) {
        log.info("Persistindo ExecFile no banco:");
        log.info(execFile.toString());
        execFile = dao.save(execFile);

        log.info("ExecFile ID {} gravado com sucesso.", execFile.getId());
        return execFile;
    }

}
