package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.master.MasterService;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Service
public class UserService extends MasterService<Integer, UserEntity> {

    private final UserRepository dao;

    public UserService(UserRepository dao) {
        super(dao);
        this.dao = dao;
    }

    @ConsoleLog
    public UserEntity addUser(@NotNull UserEntity user) {
        return dao.save(user);
    }

    @ConsoleLog
    public UserEntity getUserByEmail(@NotBlank String email) {
        return dao.findByEmail(email)
                  .orElseThrow();
    }

}
