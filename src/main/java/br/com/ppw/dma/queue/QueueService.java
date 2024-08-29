package br.com.ppw.dma.queue;

import br.com.ppw.dma.execFile.ExecFileService;
import br.com.ppw.dma.execQuery.ExecQueryService;
import br.com.ppw.dma.master.MasterService;
import com.google.gson.Gson;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class QueueService extends MasterService<Long, Queue, QueueService> {

    private final QueueRepository queueDao;
    private final ExecFileService execFileService;
    private final ExecQueryService execQueryService;
    private Gson gson;

    @Autowired
    public QueueService(
        QueueRepository queueDao,
        ExecFileService execFileService,
        ExecQueryService execQueryService,
        Gson gson) {
        //---------------------------------------
        super(queueDao);
        this.queueDao = queueDao;
        this.execFileService = execFileService;
        this.execQueryService = execQueryService;
        this.gson = gson;
    }

    @Transactional
    public Queue persist(@NotNull Queue queue) {
        log.info("Persistindo Queue no banco:");
        log.info(queue.toString());
        queue = queueDao.save(queue);

        log.info("Evidência ID {} gravado com sucesso.", queue.getId());
        return queue;
    }

}
