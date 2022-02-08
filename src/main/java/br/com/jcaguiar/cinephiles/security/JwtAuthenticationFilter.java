package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserEntity;
import br.com.jcaguiar.cinephiles.user.UserService;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
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
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtAuthenticationService jwtService;
    private final static String NO_AUTH_HEADER = "Request without 'authorization' header";
    private final static String NO_BEARER_TOKEN = "Request's 'authorization' header doesn't contain 'bearer'";

    JwtAuthenticationFilter(UserService userService, JwtAuthenticationService jwtService)
    {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException
    {
        final String bearerToken = getBearerToken(request);
        final UserEntity user = jwtService.decodeToken(bearerToken);
        final Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
        //try {
        //} catch (NoSuchElementException e) {
        //    System.out.println("Request header doesn't provide 'Authorization' attribute");
        //}  catch (NullPointerException e) {
        //    System.out.println("Request header doesn't provide Bearer token");
        //} catch (Exception e) {
        //    System.out.println("Unexpected error while getting 'Authorization' header from the request");
        //    e.printStackTrace();
        //}
    }

    private String getBearerToken(HttpServletRequest request)
    {
        final String header = Optional.ofNullable(request.getHeader("Authorization"))
            .orElse(noAuthHeader());
        if (header.startsWith("Bearer")) {
            return header.split("Bearer")[0].trim();
        }
        return noBearerToken();
    }
    //private String getBearerToken (HttpServletRequest request)
    //throws BearerTokenException, AuthorizationHeaderException {
    //    final String header = Optional.ofNullable(request.getHeader("Authorization"))
    //        .orElseThrow(() -> new AuthorizationHeaderException(HttpStatus.UNAUTHORIZED, request));
    //    if(header.startsWith("Bearer")) { return header.split("Bearer")[0].trim(); }
    //    throw new BearerTokenException(HttpStatus.UNAUTHORIZED, request);
    //}

    private static String noAuthHeader()
    {
        System.out.println(NO_AUTH_HEADER);
        return NO_AUTH_HEADER;
    }

    private static String noBearerToken()
    {
        System.out.println(NO_BEARER_TOKEN);
        return NO_BEARER_TOKEN;
    }

}
