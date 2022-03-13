package br.com.jcaguiar.cinephiles.movie;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@Entity(name = "posters")
@Table(name = "posters")
public class PostersEntity extends PostersModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    MovieModel movie;

    public PostersEntity addMovie(MovieEntity movie) {
        movie.addPosters(this);
        return this;
    }

    public PostersEntity addMovie(List<MovieEntity> movies) {
        movies.forEach(poster -> poster.addPosters(this));
        return this;
    }

}
