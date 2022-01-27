package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<MovieEntity, Integer> {

    Page<MovieEntity> findByGenre(GenreEnum genre, Pageable pageable);

    Page<MovieEntity> findByTitle(String title, Pageable pageable);

    Page<MovieEntity> findAllLike(Example<MovieEntity> movieEx, Pageable pageable);

    Page<MovieEntity> findBySynopsis(String synopsis, Pageable pageable);

    Page<MovieEntity> findByActorsLike(String actor, Pageable pageable);

    Page<MovieEntity> findByDirectorsLike(String director, Pageable pageable);

    Page<MovieEntity> findByProducersLike(String producer, Pageable pageable);
}
