package br.com.ppw.dma.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestIdFilter extends OncePerRequestFilter {

    public static final String MDC_REQUEST_ID = "requestId";

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
    throws ServletException, IOException {
        try {
            String requestId = UUID.randomUUID().toString();
            MDC.put(MDC_REQUEST_ID, requestId);
            System.out.println("   !LOG! -- Adicionando request ID: " + requestId);
            filterChain.doFilter(request, response);
        }
        finally {
            System.out.println("   !LOG! -- Removendo request ID");
            MDC.remove(MDC_REQUEST_ID);
        }
    }

}
