package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthenticationService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        try { return userService.getUserByEmail(email); }
        catch (Exception e) {throw new AuthenticationServiceException("Invalid email or password"); }
    }
}
