package br.com.jcaguiar.cinephiles.security;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
public class LoginDtoRequest {

    @NotNull
    @NotEmpty
    @Length(min = 7)
    private String email;

    @NotNull
    @NotEmpty
    @Length(min = 7)
    private String password;
}
