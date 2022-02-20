package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.master.MasterEntity;
import br.com.jcaguiar.cinephiles.master.MasterRecord;
import br.com.jcaguiar.cinephiles.people.ActorEntity;
import br.com.jcaguiar.cinephiles.people.DirectorEntity;
import br.com.jcaguiar.cinephiles.people.ProducerEntity;
import br.com.jcaguiar.cinephiles.user.WatchpointsEntity;
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

    public MovieEntity addDirector(DirectorEntity director) {
        getDirectors().add(director);
        return this;
    }

    public MovieEntity addActor(ActorEntity actor) {
        getActors().add(actor);
        return this;
    }

    public MovieEntity addProctor(ProducerEntity producer) {
        getProducers().add(producer);
        return this;
    }
}
