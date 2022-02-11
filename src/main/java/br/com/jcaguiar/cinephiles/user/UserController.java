package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @ConsoleLog
    @GetMapping
    public ResponseEntity<?> test() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @ConsoleLog
    @PostMapping("/test")
    public ResponseEntity<?> test2() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @ConsoleLog
    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> add(@Valid @RequestBody UserDtoRequest userRequest) {
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        userRequest.setPassword( encoder.encode(userRequest.getPassword()) );
        final UserEntity user = modelMapper.map(userRequest, UserEntity.class);
        userService.addUser(user);
        final UserDtoResponse userResponse = modelMapper.map(user, UserDtoResponse.class);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }
}
