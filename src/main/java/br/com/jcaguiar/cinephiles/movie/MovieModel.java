package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.AgeEnum;
import br.com.jcaguiar.cinephiles.enums.DesignEnum;
import br.com.jcaguiar.cinephiles.enums.PegiEnum;
import br.com.jcaguiar.cinephiles.enums.ScriptEnum;
import br.com.jcaguiar.cinephiles.people.ActorEntity;
import br.com.jcaguiar.cinephiles.people.DirectorEntity;
import br.com.jcaguiar.cinephiles.people.ProducerEntity;
import br.com.jcaguiar.cinephiles.util.ListConverter;
import br.com.jcaguiar.cinephiles.util.PegiEnumConvert;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
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
    Date premiereDate;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "movies_genres", joinColumns = @JoinColumn(name = "movies_id"),
               inverseJoinColumns = @JoinColumn(name = "genre_id"))
    final List<GenreEntity> genres = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable( name = "movies_directors", joinColumns = @JoinColumn(name = "movies_id"),
                inverseJoinColumns = {
                    @JoinColumn(name = "directors_first_name", referencedColumnName = "first_name"),
                    @JoinColumn(name = "directors_last_name", referencedColumnName = "last_name") })
    final List<DirectorEntity> directors = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "movies_producers", joinColumns = @JoinColumn(name = "movies_id"),
               inverseJoinColumns = {
                    @JoinColumn(name = "producers_first_name", referencedColumnName = "first_name"),
                    @JoinColumn(name = "producers_last_name", referencedColumnName = "last_name") })
    final List<ProducerEntity> producers = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable( name = "movies_actors", joinColumns = @JoinColumn(name = "movies_id"),
                inverseJoinColumns = {
                    @JoinColumn(name = "actors_first_name", referencedColumnName = "first_name"),
                    @JoinColumn(name = "actors_last_name", referencedColumnName = "last_name") })
    final List<ActorEntity> actors = new ArrayList<>();

    @Convert(converter = PegiEnumConvert.class)
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
