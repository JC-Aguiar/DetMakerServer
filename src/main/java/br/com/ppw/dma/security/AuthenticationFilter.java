//package br.com.ppw.dma.security;
//
//import br.com.ppw.dma.security.audition.Historico;
//import br.com.ppw.dma.security.audition.HistoricoRepository;
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.time.OffsetDateTime;
//
//import static br.com.ppw.dma.util.FormatDate.RELOGIO;
//
//@Slf4j
//@Component
//public class AuthenticationFilter implements Filter {
//
//    @Autowired
//    private HistoricoRepository historicoRepository;
//
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//    throws IOException, ServletException {
//        log.info("Salvando requisição no histórico da base.");
//        var dataHoraAgora = OffsetDateTime.now(RELOGIO);
//        var httpRequest = (HttpServletRequest) request;
//        var ip = httpRequest.getRemoteAddr();
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        if(auth == null) {
//            chain.doFilter(request, response);
//            return;
//        }
//        var email = auth.getName();
//        var historico = Historico.builder()
//            .endpoint(httpRequest.getRequestURI())
//            .metodo(httpRequest.getMethod())
//            .ip(ip)
//            .usuario(email)
//            .dispositivo(httpRequest.getHeader("User-Agent"))
//            .data(dataHoraAgora)
//            .build();
//        log.info(historico.toString());
//        historico = historicoRepository.save(historico);
//        log.info("ID gerado: [{}]", historico.getId());
//
//        chain.doFilter(request, response);
//    }
//}
