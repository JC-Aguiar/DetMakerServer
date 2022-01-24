package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.*;
import br.com.jcaguiar.cinephiles.master.DatedEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "movie")
final public class MovieModel extends DatedEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String title;
    String synopsis;
    GenreEnum genre;
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
    Integer views;
    Integer votes;
    Short score;
    Duration duration;
    Duration watchedTime;
    String media;


}
