package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.master.MasterRecord;
import br.com.jcaguiar.cinephiles.user.UserEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "movies")
@Table(name = "movies")
final public class MovieEntity extends MovieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    Integer views;
    Integer votes;
    Short score;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "users_id")
    final List<UserEntity> users = new ArrayList<>();

    @Embedded
    MasterRecord data;

    public MovieEntity addDirector(String director) {
        getDirectors().add(director);
        return this;
    }

    public MovieEntity addActor(String actor) {
        getActors().add(actor);
        return this;
    }

    public MovieEntity addProctor(String producer) {
        getProducers().add(producer);
        return this;
    }
}
