package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.master.MasterDtoResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;

public class UserDtoResponse extends UserModel implements MasterDtoResponse {

    @Transient
    @JsonIgnore
    String password;

}
