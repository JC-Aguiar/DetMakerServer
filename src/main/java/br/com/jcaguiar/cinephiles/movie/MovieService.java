package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository dao;

    public List<MovieEntity> getMoviesByGenre(GenreEnum genre) {
        return dao.findAllByGenre(genre);
    }

    public List<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx) {
        return dao.findAll(movieEx);
    }
}
