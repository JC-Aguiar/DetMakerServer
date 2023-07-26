package br.com.ppw.dma.movie;

import br.com.ppw.dma.company.ProducerEntity;
import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.master.MasterRecord;
import br.com.ppw.dma.people.ActorEntity;
import br.com.ppw.dma.people.DirectorEntity;
import br.com.ppw.dma.user.WatchpointsEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@Entity(name = "movies")
@Table(name = "movies")
final public class MovieEntity extends MovieModel implements MasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;
    Integer views;
    Integer votes;
    Short score;

    @ToString.Exclude
    @JsonBackReference
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "id")
    @Column(name = "watchpoints_id")
    final List<WatchpointsEntity> moviesWatchpoints = new ArrayList<>();

    @Embedded
    MasterRecord data;

    public MovieEntity addGenres(GenreEntity genre) {
        getGenres().add(genre);
        return this;
    }

    public MovieEntity addGenres(List<GenreEntity> genres) {
        getGenres().addAll(genres);
        return this;
    }

    public MovieEntity addDirector(DirectorEntity director) {
        getDirectors().add(director);
        return this;
    }

    public MovieEntity addDirector(List<DirectorEntity> directors) {
        getDirectors().addAll(directors);
        return this;
    }

    public MovieEntity addActor(ActorEntity actor) {
        getActors().add(actor);
        return this;
    }

    public MovieEntity addActor(List<ActorEntity> actors) {
        getActors().addAll(actors);
        return this;
    }

    public MovieEntity addProducers(ProducerEntity producer) {
        getProducers().add(producer);
        return this;
    }

    public MovieEntity addProducers(List<ProducerEntity> producers) {
        getProducers().addAll(producers);
        return this;
    }

    public MovieEntity addPosters(PostersEntity poster) {
        getPosters().add(poster);
        return this;
    }

    public MovieEntity addPosters(List<PostersEntity> posters) {
        getPosters().addAll(posters);
        return this;
    }


}
