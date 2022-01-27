package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<MovieEntity, Integer> {

    List<MovieEntity> findByGenre(GenreEnum genre);
    List<MovieEntity> findByTitle(String title);
    Page<MovieEntity> findByMovieEntityLike(Example<MovieEntity> movieEx, Pageable pageable);

}
