package br.com.jcaguiar.cinephiles.people;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class PeopleModel {

    @NotBlank(message = "'First Name' cant be empty")
    String firstName;

    @NotBlank(message = "'Last Name' cant be empty")
    String lastName;

}
