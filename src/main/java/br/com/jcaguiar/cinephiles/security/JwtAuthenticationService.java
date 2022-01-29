package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

public class JwtAuthenticationService {

    @Autowired
    private UserService userService;

    public JwtTokenResponse newJwtToken(Authentication auth) {
//        final UserEntity user = userService.getUserByEmail( )
        return null;
    }

}
