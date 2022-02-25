package br.com.jcaguiar.cinephiles.people;

import br.com.jcaguiar.cinephiles.master.NameableModel;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
@ToString(callSuper = true)
public class PeopleModel implements NameableModel {

    @NotBlank(message = "'First Name' cant be empty")
    @Column(name = "first_name")
    String firstName;

    @NotBlank(message = "'Last Name' cant be empty")
    @Column(name = "last_name")
    String lastName;

    @Override
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    @Override
    public String getInitialsName() {
        return String.format("%S", NameableModel.findInitials(getFullName()));
    }


}
