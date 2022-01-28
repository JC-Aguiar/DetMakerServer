package br.com.jcaguiar.cinephiles.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationProviderJWT implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        return new UsernamePasswordAuthenticationToken(
                auth.getName(),
                auth.getCredentials().toString(),
                auth.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authClass) {
        return authClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
