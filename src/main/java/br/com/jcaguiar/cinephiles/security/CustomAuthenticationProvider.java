package br.com.jcaguiar.cinephiles.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Authentication authenticate(Authentication requestedLogin) throws AuthenticationException
    {
        System.out.println("CustomAuthenticationProvider");
        final String requestUserEmail = requestedLogin.getName();
        final String requestUserPassword = requestedLogin.getCredentials().toString();
        final UserDetails user = authenticationService.loadUserByUsername(requestUserEmail);
        final Collection<? extends GrantedAuthority> userRoles = user.getAuthorities();
        final String cryptPassword = user.getPassword();
        System.out.println("GET-PRINCIPAL: " + requestUserEmail);
        final BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
        final boolean login = crypt.matches(requestUserPassword, cryptPassword);
        if(!login) throw new AuthenticationServiceException("Invalid email or password");
        return new UsernamePasswordAuthenticationToken(user, null, userRoles);
    }

    @Override
    public boolean supports(Class<?> authClass)
    {
        return authClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
