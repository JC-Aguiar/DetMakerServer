package br.com.jcaguiar.cinephiles.movie;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@Entity(name = "genres")
@Table(name = "genres")
public class GenreEntity extends GenreModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;



}
