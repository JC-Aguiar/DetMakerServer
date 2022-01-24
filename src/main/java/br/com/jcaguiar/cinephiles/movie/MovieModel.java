package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.MappedSuperclass;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PROTECTED)
@MappedSuperclass
public class MovieModel {

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
    String media;

}
