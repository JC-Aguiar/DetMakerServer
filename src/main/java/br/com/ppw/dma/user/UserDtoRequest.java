package br.com.ppw.dma.user;

import br.com.ppw.dma.master.MasterDtoRequest;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
@RequiredArgsConstructor
@ToString(callSuper = true)
public class UserDtoRequest implements MasterDtoRequest {

    @NotBlank(message = "Insert valid email")
    @Email(message = "Insert valid email")
    String email;

    @NotBlank(message = "'Password' cant be empty")
    String password;

}
