package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository dao;

    public MovieEntity getMovieById(Integer id) {
        return Optional.ofNullable(dao.getById(id)).orElseThrow();
    }

    public List<MovieEntity> getMoviesByGenre(GenreEnum genre) {
        return dao.findByGenre(genre);
    }

    public Page<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx, Pageable pageable) {
        return dao.findByMovieEntityLike(movieEx, pageable);
    }

    public List<MovieEntity> getMoviesByTitle(String title) {
        return dao.findByTitle(title);
    }
}
