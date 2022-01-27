package br.com.jcaguiar.cinephiles.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    //TODO: poderia ser static?
    @Autowired
    private UserRepository dao;

    public UserEntity getUserByEmail(String email) {
        return dao.findByEmail(email).orElseThrow();
    }

}
