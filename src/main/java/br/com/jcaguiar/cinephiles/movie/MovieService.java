package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService extends MasterService<Integer, MovieEntity> {

    @Autowired
    private MovieRepository dao;

    public MovieService(MovieRepository dao)
    {
        super(dao);
    }

    public Page<MovieEntity> getMoviesByGenre(GenreEnum genre, Pageable pageable) {
        return pageCheck(dao.findByGenre(genre, pageable));
    }

    public Page<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx, Pageable pageable) {
        return pageCheck(dao.findAll(movieEx, pageable));
    }

    public Page<MovieEntity> getMoviesByTitle(String title, Pageable pageable) {
        return pageCheck(dao.findByTitle(title, pageable));
    }

    public Page<MovieEntity> getMoviesBySynopsis(String synopsis, Pageable pageable) {
        return pageCheck(dao.findBySynopsis(synopsis, pageable));
    }

    public Page<MovieEntity> getMoviesByActor(String actor, Pageable pageable) {
        final List<String> teste = new ArrayList<>();
        teste.add(actor);
        return pageCheck(dao.findByActorsIn(teste, pageable));
    }
//
//    public Page<MovieEntity> getMoviesByActor(List<String> actors, Pageable pageable) {
//        System.out.println(actors.getClass().getName().toString());
//        return pageCheck(dao.getByActorsIn(actors, pageable));
//    }

    public Page<MovieEntity> getMoviesByDirector(String director, Pageable pageable) {
        return pageCheck(dao.findByDirectorsLike(director, pageable));
    }

    public Page<MovieEntity> getMoviesByProducer(String producer, Pageable pageable) {
        return pageCheck(dao.findByProducersLike(producer, pageable));
    }
}
