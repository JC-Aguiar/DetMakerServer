package br.com.ppw.dma.user;

import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.util.ConsoleLog;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Service
public class UserService extends MasterService<Integer, UserEntity, UserService> {

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
