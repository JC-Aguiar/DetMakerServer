package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.master.MasterService;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Service
public class UserService extends MasterService<Integer, UserEntity> {

    //TODO: poderia ser static?
    @Autowired
    private UserRepository dao;

    public UserService(JpaRepository<UserEntity, Integer> dao) {
        super(dao);
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
