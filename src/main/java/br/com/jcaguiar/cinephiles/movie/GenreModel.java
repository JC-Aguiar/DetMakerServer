package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.MappedSuperclass;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class GenreModel {

    GenreEnum genre;

}
