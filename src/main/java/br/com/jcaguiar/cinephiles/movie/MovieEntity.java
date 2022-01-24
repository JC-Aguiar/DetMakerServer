package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.master.MasterRecord;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.Duration;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "movie")
final public class MovieEntity extends MovieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;


    Integer views;
    Integer votes;
    Short score;

    @Embedded
    MasterRecord data;
}
