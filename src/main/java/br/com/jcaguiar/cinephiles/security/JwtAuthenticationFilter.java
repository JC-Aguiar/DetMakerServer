package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.exception.AuthorizationHeaderException;
import br.com.jcaguiar.cinephiles.exception.BearerTokenException;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import br.com.jcaguiar.cinephiles.util.ConsoleLogAspect;
import io.jsonwebtoken.JwtException;
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
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtService;

    public JwtAuthenticationFilter(JwtAuthenticationService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    @ConsoleLog
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        //TODO: System.out.println("JwtAuthenticationFilter");
        final String uri = request.getRequestURI();
        final String endpoint = (String) Arrays.stream(uri.split("/"))
                .filter(s -> !s.isBlank())
                .toArray()[0];
        //TODO: System.out.println("URI PATH: " + uri);
        //TODO: System.out.println("END-POINT: " + endpoint);
        final boolean restrictedAccess = WebSecurityConfig.DOMAINS.containsKey(endpoint);
        //TODO: System.out.printf("ACCESS: %s \n", restrictedAccess ? "restricted" : "free");
        try { authenticateToken(request); }
        catch (AuthorizationHeaderException | JwtException | IllegalArgumentException e) {
            if (restrictedAccess) { throw e; }
            else { ConsoleLogAspect.LOGGER.error(e.getLocalizedMessage()); }
        }
        catch (Exception e) { throw e; }
        finally { filterChain.doFilter(request, response); }
    }

    @ConsoleLog
    private void authenticateToken(HttpServletRequest request)
    throws AuthorizationHeaderException, JwtException {
        //TODO: System.out.println("authenticateToken");
        final String bearerToken = getBearerToken(request);
        final Authentication auth = jwtService.decodeToken(bearerToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        //TODO: System.out.println("User authenticated!");
    }

    @ConsoleLog
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
