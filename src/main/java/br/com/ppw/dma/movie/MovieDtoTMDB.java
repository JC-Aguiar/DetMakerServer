package br.com.ppw.dma.movie;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

