package br.com.jcaguiar.cinephiles.people;

import br.com.jcaguiar.cinephiles.master.NameableEntity;
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
public class PeopleModel implements NameableEntity {

    @NotBlank(message = "'First Name' cant be empty")
    String firstName;

    @NotBlank(message = "'Last Name' cant be empty")
    String lastName;

    @Override
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    @Override
    public String getInitialsName() {
        return String.format("%S",
                NameableEntity.findInitials(firstName) +
                NameableEntity.findInitials(lastName));
    }


}
