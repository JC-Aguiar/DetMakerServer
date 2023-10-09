//package br.com.ppw.dma.security;
//
//import br.com.ppw.dma.user.UserDtoRequest;
//import br.com.ppw.dma.user.UserService;
//import br.com.ppw.dma.util.ConsoleLog;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.validation.Valid;
//
//@RestController
//@RequestMapping("/login")
//public class LoginController {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private JwtAuthenticationService jwtAuthenticationService;
//
//    @Autowired
//    private AuthenticationController authenticationManager;
//
//    @ConsoleLog
//    @PostMapping
//    public ResponseEntity<?> login(@RequestBody @Valid UserDtoRequest requestedLogin)
//    {
//        System.out.println("LOGIN CONTROLLER");
//        final UsernamePasswordAuthenticationToken springToken = new UsernamePasswordAuthenticationToken(
//            requestedLogin.getEmail(), requestedLogin.getPassword());
//        //Authentication logic returns Object with authenticated UserEntity (if's doesn't throws)
//        final Authentication authenticatedUser = authenticationManager.authenticate(springToken);
//        final JwtTokenResponse tokenResponse = jwtAuthenticationService.createToken(authenticatedUser);
//        return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
//    }
//
//}
