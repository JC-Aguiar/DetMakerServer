package br.com.ppw.dma.security.audition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Data
@Slf4j
@Service
public class HistoricoService {

    @Autowired
    public HistoricoRepository historicoRepository;

    @Async
    @EventListener
    @Transactional
    public void handleHistoricoEvent(HistoricoEvent event) throws InterruptedException {
        var historico = event.historico();
        log.info("Processando evento de histórico para endpoint: {}", historico.getEndpoint());
        historico = historicoRepository.save(event.historico());
        log.info("Histórico salvo com ID: [{}]", historico.getId());
    }

}
