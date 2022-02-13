package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.master.MasterDtoResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor
public class UserDtoResponse extends UserModel implements MasterDtoResponse {

    @JsonIgnore
    String password = null;

}
