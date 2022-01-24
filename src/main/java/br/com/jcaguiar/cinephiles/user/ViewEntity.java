package br.com.jcaguiar.cinephiles.user;

import br.com.jcaguiar.cinephiles.movie.MovieEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.time.Duration;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "watchpoint")
final public class ViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    Duration watchedTime;

    @ManyToOne
    UserEntity user;

    @ManyToOne
    MovieEntity movie;
}
