package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository dao;

    public MovieEntity getMovieById(Integer id) {
        return Optional.ofNullable(dao.getById(id)).orElseThrow();
    }

    public Page<MovieEntity> getMoviesByGenre(GenreEnum genre, Pageable pageable) {
        return dao.findByGenre(genre, pageable);
    }

    public Page<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx, Pageable pageable) {
        return dao.findAllLike(movieEx, pageable);
    }

    public Page<MovieEntity> getMoviesByTitle(String title, Pageable pageable) {
        return dao.findByTitle(title, pageable);
    }

    public Page<MovieEntity> getMoviesBySynopsis(String Synopsis, Pageable pageable) {
        return dao.findBySynopsis(Synopsis, pageable);
    }

    public Page<MovieEntity> getMoviesByActor(String actor, Pageable pageable) {
        return dao.findByActorsLike(actor, pageable);
    }

    public Page<MovieEntity> getMoviesByDirector(String director, Pageable pageable) {
        return dao.findByDirectorsLike(director, pageable);
    }

    public Page<MovieEntity> getMoviesByProducer(String producer, Pageable pageable) {
        return dao.findByProducersLike(producer, pageable);
    }
}
