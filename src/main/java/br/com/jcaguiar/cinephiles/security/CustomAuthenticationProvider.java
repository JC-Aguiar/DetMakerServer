package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserService userService;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException
    {
        final BCryptPasswordEncoder crypt = new BCryptPasswordEncoder();
        final String userPassword = auth.getCredentials().toString();
        final String cryptPassword = userService.getUserByEmail(auth.getCredentials().toString()).getPassword();
        final boolean login = crypt.matches(userPassword, cryptPassword);
        if(!login) throw new AuthenticationServiceException("Invalid email or password");
        return new UsernamePasswordAuthenticationToken(auth.getName(), userPassword);
    }

    @Override
    public boolean supports(Class<?> authClass)
    {
        return authClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
