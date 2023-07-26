package br.com.ppw.dma.security;

import br.com.ppw.dma.exception.AuthorizationHeaderException;
import br.com.ppw.dma.exception.BearerTokenException;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j2(topic = "JWT AUTHENTICATION FILTER")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtService;

    public JwtAuthenticationFilter(JwtAuthenticationService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        final String uri = request.getRequestURI();
        final List<String> path = Arrays.stream(uri.split("/")).filter(s -> !s.isBlank()).toList();
        final String endpoint = path.get(0);
        final String parameters = String.join(",", path.subList(1, path.size()));
        final boolean restrictedAccess = WebSecurityConfig.PROTECTED_DOMAINS.containsKey(endpoint);
        log.info("Request URI: {}", uri);
        log.info("Request Endpoint: {} {}", endpoint, parameters);
        log.info("Request Access: {}", restrictedAccess ? "restricted" : "free");
        try {
            authenticateToken(request);
        } catch (AuthorizationHeaderException | JwtException | IllegalArgumentException e) {
            if (restrictedAccess) { throw e; }
            else { log.warn(e.getLocalizedMessage()); }
        } finally { filterChain.doFilter(request, response); }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    private void authenticateToken(HttpServletRequest request)
    throws AuthorizationHeaderException, JwtException {
        //TODO: System.out.println("authenticateToken");
        final String bearerToken = getBearerToken(request);
        final Authentication auth = jwtService.decodeToken(bearerToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        //TODO: System.out.println("User authenticated!");
    }

    private String getBearerToken(HttpServletRequest request) {
        //TODO: System.out.println("getBearerToken");
        final String header = Optional.ofNullable(
                        request.getHeader(HttpHeaders.AUTHORIZATION))
                .orElseThrow(AuthorizationHeaderException::new);
        //TODO: System.out.println("AUTHORIZATION HEADER: " + header);
        if (header.startsWith("Bearer")) {
            return header.replace("Bearer", "").trim();
        }
        throw new BearerTokenException();
    }

}
