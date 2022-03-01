package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.exception.AuthorizationHeaderException;
import br.com.jcaguiar.cinephiles.exception.BearerTokenException;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import io.jsonwebtoken.JwtException;
import lombok.SneakyThrows;
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

    public JwtAuthenticationFilter(JwtAuthenticationService jwtService)
    {
        this.jwtService = jwtService;
    }

    @SneakyThrows
    @Override
    @ConsoleLog
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException
    {
        System.out.println("JwtAuthenticationFilter");
        final String uri = request.getRequestURI();
        final String endpoint = (String) Arrays.stream(uri.split("/"))
            .filter(s -> !s.isBlank())
            .toArray()[0];
        System.out.println("URI PATH: " + uri);
        System.out.println("END-POINT: " + endpoint);
        final boolean restrictedAccess = WebSecurityConfig.DOMAINS.containsKey(endpoint);
        System.out.printf("ACCESS: %s \n", restrictedAccess ? "restricted" : "free");
        try { authenticateToken(request); }
        catch (AuthorizationHeaderException | JwtException | IllegalArgumentException e) {
            if(restrictedAccess) { throw e; }
            else { System.out.println(e.getLocalizedMessage()); }
        }
        catch (Exception e) { throw e; }
        finally { filterChain.doFilter(request, response); }
    }

    @ConsoleLog
    private void authenticateToken(HttpServletRequest request)
    throws AuthorizationHeaderException, JwtException
    {
        System.out.println("authenticateToken");
        final String bearerToken = getBearerToken(request);
        final Authentication auth = jwtService.decodeToken(bearerToken);
        SecurityContextHolder.getContext().setAuthentication(auth);
        System.out.println("User authenticated!");
    }

    @ConsoleLog
    private String getBearerToken(HttpServletRequest request)
    {
        System.out.println("getBearerToken");
        final String header = Optional.ofNullable(
            request.getHeader(HttpHeaders.AUTHORIZATION))
            .orElseThrow(AuthorizationHeaderException::new);
        System.out.println("AUTHORIZATION HEADER: " + header);
        if (header.startsWith("Bearer")) {
            return header.replace("Bearer", "").trim();
        }
        throw new BearerTokenException();
    }

}
