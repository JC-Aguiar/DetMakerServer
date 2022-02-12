package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    //TODO: poderia ser static?
    @Autowired
    private UserRepository dao;

    @ConsoleLog
    public UserEntity addUser(@NotNull UserEntity user) {
        return dao.save(user);
    }

    @ConsoleLog
    public UserEntity getUserById(@Positive @NotNull int id) {
        return Optional.ofNullable(dao.getById(id)).orElseThrow();
    }

    @ConsoleLog
    public UserEntity getUserByEmail(@NotBlank String email) {
        return dao.findByEmail(email).orElseThrow();
    }

    public Page<UserEntity> findAll(@NotNull Pageable pageable) {
        final Page<UserEntity> usersEntities = dao.findAll(pageable);
        usersEntities.stream().filter(Objects::nonNull).findFirst().orElseThrow();
        return usersEntities;
    }
}
