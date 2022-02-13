package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @ConsoleLog
    @GetMapping
    public ResponseEntity<?> getAll() {
        final Pageable pageConfig = PageRequest.of(0, 12, Sort.by("id").ascending());
        final Page<UserEntity> usersEntities = userService.findAll(pageConfig);
        usersEntities.stream().filter(Objects::nonNull).findFirst().orElseThrow();
        final Page<UserDtoResponse> usersResponse = usersEntities.map(
                user -> modelMapper.map(user, UserDtoResponse.class));
        return new ResponseEntity<>(usersResponse, HttpStatus.OK);

        //TODO: Create handler controller for NoSuchElementException
        // -> ResponseStatusException(HttpStatus.NOT_FOUND, "No registers in database")

    }

    @ConsoleLog
    @Transactional
    @PostMapping("/add")
    public ResponseEntity<?> add(@Valid @RequestBody UserDtoRequest userRequest) {
        final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        final UserEntity user = modelMapper.map(userRequest, UserEntity.class);
        user.setPassword( encoder.encode(userRequest.getPassword()) );
        userService.addUser(user);
        final UserDtoResponse userResponse = modelMapper.map(user, UserDtoResponse.class);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }
}
