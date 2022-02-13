package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.*;
import br.com.jcaguiar.cinephiles.util.ListConverter;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class MovieModel {

    @Column(unique = true)
    @NotBlank(message = "'Title' cant be empty")
    String title;
    String synopsis;

    @Convert(converter = ListConverter.class)
    final List<GenreEnum> genre = new ArrayList<>();
    Date premiereDate;

    @Convert(converter = ListConverter.class)
    final List<String> directors = new ArrayList<>();

    @Convert(converter = ListConverter.class)
    final List<String> producers = new ArrayList<>();

    @Convert(converter = ListConverter.class)
    final List<String> actors = new ArrayList<>();
    PegiEnum ageRange;
    String logo;
    String posters;
    String font;
    String position;
    String fit;

    @Convert(converter = ListConverter.class)
    final List<AgeEnum> age = new ArrayList<>();
    ScriptEnum script;

    @Convert(converter = ListConverter.class)
    final List<DesignEnum> design = new ArrayList<>();
    Duration duration;

    @URL
    String media;

}
