package br.com.jcaguiar.cinephiles.movie;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class MovieDtoTMDB {

    String title;
    String overview;
    String tagline;
    String release_date;
    List<MovieDtoTMDBGenre> genres;
    List<MovieDtoTMDBProductors> production_companies;
    String poster_path;
    String runtime;

}

