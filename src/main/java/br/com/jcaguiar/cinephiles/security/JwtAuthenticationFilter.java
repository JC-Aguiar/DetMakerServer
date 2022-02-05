package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.exception.AuthorizationHeaderException;
import br.com.jcaguiar.cinephiles.exception.BearerTokenException;
import br.com.jcaguiar.cinephiles.user.UserEntity;
import br.com.jcaguiar.cinephiles.user.UserService;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtAuthenticationService jwtService;

    JwtAuthenticationFilter(UserService userService, JwtAuthenticationService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
            final String bearerToken = getBearerToken(request);
            final String userEmail = "";
            final UserEntity user = userService.getUserByEmail(userEmail);
//        try {
//        } catch (NoSuchElementException e) {
//            System.out.println("Request header doesn't provide 'Authorization' attribute");
//        }  catch (NullPointerException e) {
//            System.out.println("Request header doesn't provide Bearer token");
//        } catch (Exception e) {
//            System.out.println("Unexpected error while getting 'Authorization' header from the request");
//            e.printStackTrace();
//        }
    }

    private String getBearerToken (HttpServletRequest request)
    throws BearerTokenException, AuthorizationHeaderException {
        final String header = Optional.ofNullable(request.getHeader("Authorization"))
            .orElseThrow(() -> new AuthorizationHeaderException(HttpStatus.UNAUTHORIZED, request));
        if(header.startsWith("Bearer")) {
            return header.split("Bearer")[0].trim();
        }
        throw new BearerTokenException(HttpStatus.UNAUTHORIZED, request);
    }
}
