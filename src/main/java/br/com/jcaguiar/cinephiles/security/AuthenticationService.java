package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2(topic = "AUTHENTICATION SERVICE")
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String credentials) throws UsernameNotFoundException
    {
        final String requestUserEmail = credentials.split(":")[0];
        final String requestUserPassword = credentials.split(":")[1];
        log.info("Parameters: {}", credentials);
        try {
            final UserDetails user = userService.getUserByEmail(requestUserEmail);
            log.info("E-mail approved");
            final String cryptPassword = user.getPassword();
            final boolean login = new BCryptPasswordEncoder().matches(requestUserPassword, cryptPassword);
            if(!login) { throw new AuthenticationServiceException("Invalid email or password"); }
            log.info("Password approved");
            return user;
        } catch (AuthenticationServiceException e) {
            log.warn("Invalid email or password");
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }
}
