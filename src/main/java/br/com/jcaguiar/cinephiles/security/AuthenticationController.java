package br.com.jcaguiar.cinephiles.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Log4j2(topic = "AUTHENTICATION CONTROLLER")
public class AuthenticationController implements AuthenticationProvider {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public Authentication authenticate(Authentication requestedLogin) throws AuthenticationException {
        log.info("***NEW REQUEST RECEIVED***");
        final String requestUserEmail = requestedLogin.getName();
        final String requestUserPassword = requestedLogin.getCredentials().toString();
        final String userCredentials = String.format("%s:%s", requestUserEmail, requestUserPassword);
        final UserDetails user = authenticationService.loadUserByUsername(userCredentials);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authClass) {
        return authClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
