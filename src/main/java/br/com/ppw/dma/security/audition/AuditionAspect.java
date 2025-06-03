package br.com.ppw.dma.security.audition;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;
import java.util.Optional;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Slf4j
@Aspect
@Component
public class AuditionAspect {

    @Autowired
    private HistoricoRepository historicoRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @Before("within(@org.springframework.web.bind.annotation.RestController *) || " +
            "within(@org.springframework.stereotype.Controller *)")
    public void logRequest() {
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
            .map(r -> (ServletRequestAttributes) r)
            .map(ServletRequestAttributes::getRequest)
            .ifPresent(r -> {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if(auth == null) return;

                var dataHoraAgora = OffsetDateTime.now(RELOGIO);
                var ip = r.getRemoteAddr();
                var email = auth.getName();
                var historico = Historico.builder()
                    .endpoint(r.getRequestURI())
                    .metodo(r.getMethod())
                    .ip(ip)
                    .usuario(email)
                    .dispositivo(r.getHeader("User-Agent"))
                    .data(dataHoraAgora)
                    .build();
                log.info(historico.toString());
                eventPublisher.publishEvent(new HistoricoEvent(historico));
            });
    }
}
