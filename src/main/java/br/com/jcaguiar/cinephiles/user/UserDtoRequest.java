package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.master.MasterDtoRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

@Value
public class UserDtoRequest extends UserModel implements MasterDtoRequest {

    public UserDtoRequest(String email, String firstName, String lastName, String password)  {
        super.toBuilder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(password)
                .build();
    }

}
