package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserEntity;
import br.com.jcaguiar.cinephiles.user.UserService;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException
    {
        try {
            final String bearerToken = getBearerToken(request);
            final String userEmail = "";
            final UserEntity user = userService.getUserByEmail(userEmail);
        } catch (NoSuchElementException e) {
            System.out.println("Request header doesn't provide 'Authorization' attribute");
        }  catch (NullPointerException e) {
            System.out.println("Request header doesn't provide Bearer token");
        } catch (Exception e) {
            System.out.println("Unexpected error while getting 'Authorization' header from the request");
            e.printStackTrace();
        }
    }

    private String getBearerToken (HttpServletRequest request)
    {
        final String header = Optional.ofNullable(request.getHeader("Authorization")).orElseThrow();
        if(header.startsWith("Bearer")) {
            return header.split("Bearer")[0].trim();
        }
        return null;
    }
}
