package br.com.jcaguiar.cinephiles.company;

import br.com.jcaguiar.cinephiles.master.NameableEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;
import java.util.Arrays;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class CompanyModel implements NameableEntity {

    String name;

    @Override
    public String getFullName() {
        return name.split(" ")[0];
    }

    @Override
    public String getInitialsName() {
        final boolean compositeName = name.contains(" ");
        return compositeName ?
                Arrays.stream(name.split(" ")).map(i -> i.charAt(0)).toString()
                : NameableEntity.findInitialsInSoloName(name);

    }
}
