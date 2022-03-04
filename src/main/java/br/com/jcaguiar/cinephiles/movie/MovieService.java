package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MovieService extends MasterService<Integer, MovieEntity> {

    private final MovieRepository dao;

    public MovieService(MovieRepository dao) {
        super(dao);
        this.dao = dao;
    }

    public Page<MovieEntity> getMoviesByGenre(GenreEnum genre, Pageable pageable) {
        return PROXY().pageCheck(dao.findByGenres(genre, pageable));
    }

    public Page<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx, Pageable pageable) {
        return PROXY().pageCheck(dao.findAll(movieEx, pageable));
    }

    public Page<MovieEntity> getMoviesByTitle(String title, Pageable pageable) {
        return PROXY().pageCheck(dao.findByTitle(title, pageable));
    }

    public Page<MovieEntity> getMoviesBySynopsis(String synopsis, Pageable pageable) {
        return PROXY().pageCheck(dao.findBySynopsis(synopsis, pageable));
    }

    public Page<MovieEntity> getMoviesByTextLike(String text, Pageable pageable) {
        return PROXY().pageCheck(dao.findByKeyword(text, pageable));
    }

    public Page<MovieEntity> getMoviesByActor(String actor, Pageable pageable) {
        return PROXY().pageCheck(dao.findByActorsLike(actor, pageable));
    }

    public Page<MovieEntity> getMoviesByDirector(String director, Pageable pageable) {
        return PROXY().pageCheck(dao.findByDirectorsLike(director, pageable));
    }

    public Page<MovieEntity> getMoviesByProducer(String producer, Pageable pageable) {
        return PROXY().pageCheck(dao.findByProducersLike(producer, pageable));
    }

    public MovieEntity addOne(MovieModel model) {
        final MovieEntity movie = (MovieEntity) model;
        return dao.save(movie);
    }

}
