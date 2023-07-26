package br.com.ppw.dma.movie;

import br.com.ppw.dma.enums.GenreEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class GenreModel {

    @NotNull(message = "Genre can't be empty")
    @Enumerated(EnumType.STRING)
    GenreEnum genre;

}
