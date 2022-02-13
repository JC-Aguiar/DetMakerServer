package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.people.PeopleModel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@MappedSuperclass
public class UserModel extends PeopleModel {

    @Column(unique = true)
    @NotBlank(message = "Insert valid email")
    @Email(message = "Insert valid email")
    String email;

    @NotBlank(message = "'Password' cant be empty")
    String password;
    String avatar;

}
