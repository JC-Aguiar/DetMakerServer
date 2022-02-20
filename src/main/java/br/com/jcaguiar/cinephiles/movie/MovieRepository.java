package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.people.ActorEntity;
import br.com.jcaguiar.cinephiles.people.DirectorEntity;
import br.com.jcaguiar.cinephiles.people.ProducerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, Integer> {

    Page<MovieEntity> findByGenres(GenreEnum genre, Pageable pageable);

    Page<MovieEntity> findByTitle(String title, Pageable pageable);

//    Page<MovieEntity> findAllLike(Example<MovieEntity> movieEx, Pageable pageable);

    Page<MovieEntity>
    findByActorsLikeOrDirectorsLikeOrProducersLikeOrTitleLike
    (String actor, String director, String producer, String title, Pageable pageable);

    Page<MovieEntity> findBySynopsis(String synopsis, Pageable pageable);

    @Query( "SELECT m FROM movies m INNER JOIN m.actors a " +
            "WHERE a.id.firstName = ?1 " +
            "OR a.id.lastName = ?1")
    Page<MovieEntity> findByActorsLike(String actor, Pageable pageable);

    @Query( "SELECT m FROM movies m INNER JOIN m.directors d " +
            "WHERE d.id.firstName = ?1 " +
            "OR d.id.lastName = ?1")
    Page<MovieEntity> findByDirectorsLike(String director, Pageable pageable);

    @Query( "SELECT m FROM movies m INNER JOIN m.producers p " +
            "WHERE p.id.firstName = ?1 " +
            "OR p.id.lastName = ?1")
    Page<MovieEntity> findByProducersLike(String producer, Pageable pageable);
}
