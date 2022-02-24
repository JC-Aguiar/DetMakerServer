package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.exception.AuthorizationHeaderException;
import br.com.jcaguiar.cinephiles.exception.BearerTokenException;
import br.com.jcaguiar.cinephiles.user.UserEntity;
import br.com.jcaguiar.cinephiles.user.UserService;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtAuthenticationService jwtService;
    private final Map<String, String> domains;

    public JwtAuthenticationFilter(UserService userService,
                                   JwtAuthenticationService jwtService,
                                   Map<String, String> domains)
    {
        this.userService = userService;
        this.jwtService = jwtService;
        this.domains = domains;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException
    {
        final String uri = request.getRequestURI();
        final String uriString = uri.replace("/", "");
        System.out.println("URI: " + uri);
        System.out.println("DOMAINS: ");
        domains.keySet().forEach(System.out::println);
        final boolean restrictedAccess = domains.containsKey(uriString);
//        if (!restrictedAccess) {
//            System.out.println("ACCESS: free");
//        }
//        else {
//            System.out.println("ACCESS: restricted");
            authenticateToken(request);
//        }
        filterChain.doFilter(request, response);
    }

    private void authenticateToken(HttpServletRequest request)
    {
        try {
            final String bearerToken = getBearerToken(request);
            final UserEntity user = jwtService.decodeToken(bearerToken);
            final Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getBearerToken(HttpServletRequest request)
    {
        final String header = Optional.ofNullable(
            request.getHeader("Authorization"))
            .orElseThrow(AuthorizationHeaderException::new);
        if (header.startsWith("Bearer")) {
            return header.split("Bearer")[0].trim();
        }
        throw new BearerTokenException();
    }

}
