package br.com.ppw.dma.company;

import br.com.ppw.dma.master.NameableModel;
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
public class ProducerModel implements NameableModel {

    @NotBlank(message = "Insert a valid company name")
    @Column(unique = true)
    String name;

    @Override
    public String getFullName() {
        return name.split(" ")[0];
    }

    @Override
    public String getInitialsName() {
        return name.contains(" ")
            ? NameableModel.findInitualsInComposeName(name)
            : NameableModel.findInitialsInSoloName(name);

    }
}
