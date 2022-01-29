package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
@MappedSuperclass
public class MovieModel {


    @Column(unique = true)
    @NotBlank(message = "'Title' cant be empty")
    String title;
    String synopsis;
    final List<GenreEnum> genre = new ArrayList<>();
    Date premiereDate;
    final List<String> directors = new ArrayList<>();
    final List<String> producers = new ArrayList<>();
    final List<String> actors = new ArrayList<>();
    PegiEnum ageRange;
    String logo;
    String posters;
    String font;
    String position;
    String fit;
    final List<AgeEnum> age = new ArrayList<>();
    ScriptEnum script;
    final List<DesignEnum> design = new ArrayList<>();
    Duration duration;
    @URL String media;

}
