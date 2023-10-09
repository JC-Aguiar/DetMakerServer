package br.com.ppw.dma.security;

import br.com.ppw.dma.exception.AuthorizationHeaderException;
import br.com.ppw.dma.exception.BearerTokenException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2(topic = "JWT AUTHENTICATION FILTER")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

//    private final JwtAuthenticationService jwtService;

//    public JwtAuthenticationFilter(JwtAuthenticationService jwtService) {
//        this.jwtService = jwtService;
//    }

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
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        final String uri = request.getRequestURI();
        final List<String> path = Arrays.stream(uri.split("/"))
            .filter(s -> !s.isBlank())
            .toList();
        final String endpoint = path.get(0);
        final String parameters = String.join(",", path.subList(1, path.size()));
        final boolean restrictedAccess = WebSecurityConfig.PROTECTED_DOMAINS.containsKey(endpoint);

        log.info("Request URI: {}", uri);
        log.info("Request Endpoint: {} {}", endpoint, parameters);
        log.info("Request Access: {}", restrictedAccess ? "restricted" : "free");
        try {
            authenticateToken(request);
        }
        catch (AuthorizationHeaderException | JwtException | IllegalArgumentException e) {
            if(restrictedAccess) { throw e; }
            else { log.warn(e.getLocalizedMessage()); }
        }
        finally { filterChain.doFilter(request, response); }
    }

    private void authenticateToken(HttpServletRequest request)
    throws AuthorizationHeaderException, JwtException {
        throw new RuntimeException("Método não implementado ainda");
//        final String bearerToken = getBearerToken(request);
//        final Authentication auth = jwtService.decodeToken(bearerToken);
//        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String getBearerToken(HttpServletRequest request) {
        final String header = Optional.ofNullable(
            request.getHeader(HttpHeaders.AUTHORIZATION))
            .orElseThrow(AuthorizationHeaderException::new);

        if(header.startsWith("Bearer")) {
            return header.replace("Bearer", "").trim();
        }
        throw new BearerTokenException();
    }

}
