package br.com.jcaguiar.cinephiles.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
@MappedSuperclass
public class UserModel {

    @Column(unique = true)
    @NotBlank(message = "Insert valid email")
    @Email(message = "Insert valid email")
    String email;

    @NotBlank(message = "'First Name' cant be empty")
    String firstName;

    @NotBlank(message = "'Last Name' cant be empty")
    String lastName;

    @NotBlank(message = "'Password' cant be empty")
    String password;
    String avatar;

}
