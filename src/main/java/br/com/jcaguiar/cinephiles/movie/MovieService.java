package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MovieService extends MasterService<Integer, MovieEntity> {

    @Autowired
    private MovieRepository dao;

    public MovieService(MovieRepository dao)
    {
        super(dao);
    }

    public Page<MovieEntity> getMoviesByGenre(GenreEnum genre, Pageable pageable)
    {
        return pageCheck(dao.findByGenres(genre, pageable));
    }

    public Page<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx, Pageable pageable)
    {
        return pageCheck(dao.findAll(movieEx, pageable));
    }

    public Page<MovieEntity> getMoviesByTitle(String title, Pageable pageable)
    {
        return pageCheck(dao.findByTitle(title, pageable));
    }

    public Page<MovieEntity> getMoviesBySynopsis(String synopsis, Pageable pageable)
    {
        return pageCheck(dao.findBySynopsis(synopsis, pageable));
    }

    public Page<MovieEntity> getMoviesByTextLike(String text, Pageable pageable)
    {
        return pageCheck(dao.findByKeyword(text, pageable));
    }

    public Page<MovieEntity> getMoviesByActor(String actor, Pageable pageable) {
        return (Page<MovieEntity>) pageCheck(dao.findByActorsLike(actor, pageable));
    }

    public Page<MovieEntity> getMoviesByDirector(String director, Pageable pageable) {
        return (Page<MovieEntity>) pageCheck(dao.findByDirectorsLike(director, pageable));
    }

    public Page<MovieEntity> getMoviesByProducer(String producer, Pageable pageable) {
        return (Page<MovieEntity>) pageCheck(dao.findByProducersLike(producer, pageable));
    }
}
