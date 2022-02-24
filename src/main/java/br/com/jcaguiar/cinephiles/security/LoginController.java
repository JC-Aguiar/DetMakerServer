package br.com.jcaguiar.cinephiles.security;

import br.com.jcaguiar.cinephiles.user.UserDtoRequest;
import br.com.jcaguiar.cinephiles.user.UserService;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtAuthenticationService jwtAuthenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @ConsoleLog
    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginDtoRequest requestedLogin)
    {
        System.out.println("LOGIN CONTROLLER");
        final UsernamePasswordAuthenticationToken springToken = new UsernamePasswordAuthenticationToken(
            requestedLogin.getEmail(), requestedLogin.getPassword());
        //Authentication logic returns Object with authenticated UserEntity (if's doesn't throws)
        final Authentication authenticatedUser = authenticationManager.authenticate(springToken);
        final JwtTokenResponse tokenResponse = jwtAuthenticationService.createToken(authenticatedUser);
        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
    }

}
