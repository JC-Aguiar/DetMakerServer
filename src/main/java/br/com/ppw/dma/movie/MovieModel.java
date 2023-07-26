package br.com.ppw.dma.movie;

import br.com.ppw.dma.company.ProducerEntity;
import br.com.ppw.dma.enums.AgeEnum;
import br.com.ppw.dma.enums.DesignEnum;
import br.com.ppw.dma.enums.PegiEnum;
import br.com.ppw.dma.enums.ScriptEnum;
import br.com.ppw.dma.people.ActorEntity;
import br.com.ppw.dma.people.DirectorEntity;
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

    @Column(length = 999)
    String synopsis;

    String tagline;

    Date premiereDate;

    //tees
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "movies_genres",
        joinColumns = @JoinColumn(name = "movies_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id"))
    final List<GenreEntity> genres = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "movies_directors",
        joinColumns = @JoinColumn(name = "movies_id"),
        inverseJoinColumns = {
            @JoinColumn(name = "directors_first_name", referencedColumnName = "first_name"),
            @JoinColumn(name = "directors_last_name", referencedColumnName = "last_name") })
    final List<DirectorEntity> directors = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "movies_producers",
        joinColumns = @JoinColumn(name = "movies_id"),
        inverseJoinColumns = @JoinColumn(
            name = "producer_id", referencedColumnName = "id"))
    final List<ProducerEntity> producers = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(
        name = "movies_actors",
        joinColumns = @JoinColumn(name = "movies_id"),
        inverseJoinColumns = {
            @JoinColumn(name = "actors_first_name", referencedColumnName = "first_name"),
            @JoinColumn(name = "actors_last_name", referencedColumnName = "last_name") })
    final List<ActorEntity> actors = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    PegiEnum ageRange;

    String logo;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Column(name = "posters_id")
    final List<PostersEntity> posters = new ArrayList<>();

    String font;

    String position;

    String fit;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = AgeEnum.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "movies_age", joinColumns = @JoinColumn(name = "movies_id"))
    final List<AgeEnum> movieAge = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    ScriptEnum script;

    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = DesignEnum.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "movies_design", joinColumns = @JoinColumn(name = "movies_id"))
    final List<DesignEnum> design = new ArrayList<>();

    Duration duration;

    @URL
    String media;

}
