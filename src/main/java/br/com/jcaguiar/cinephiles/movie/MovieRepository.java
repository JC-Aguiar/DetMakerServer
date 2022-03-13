package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, Integer> {

    Page<MovieEntity> findByGenres(GenreEnum genre, Pageable pageable);

    Page<MovieEntity> findByTitle(String title, Pageable pageable);

//    Page<MovieEntity> findAllLike(Example<MovieEntity> movieEx, Pageable pageable);

    @Query( "SELECT m FROM movies m " +
            "INNER JOIN m.directors d " +
            "INNER JOIN m.producers p " +
            "INNER JOIN m.actors a " +
            "WHERE m.title LIKE ?1" +
            "OR d.id.firstName LIKE ?1 OR d.id.lastName LIKE ?1 " +
            "OR p.id.firstName LIKE ?1 OR p.id.lastName LIKE ?1 " +
            "OR a.id.firstName LIKE ?1 OR a.id.lastName LIKE ?1 ")
    Page<MovieEntity> findByKeyword(String text, Pageable pageable);

    Page<MovieEntity> findBySynopsis(String synopsis, Pageable pageable);

    @Query( "SELECT m FROM movies m INNER JOIN m.actors a " +
            "WHERE a.id.firstName = ?1 OR a.id.lastName = ?1")
    Page<MovieEntity> findByActorsLike(String actor, Pageable pageable);

    @Query( "SELECT m FROM movies m INNER JOIN m.directors d " +
            "WHERE d.id.firstName LIKE ?1 OR d.id.lastName LIKE ?1")
    Page<MovieEntity> findByDirectorsLike(String director, Pageable pageable);

    @Query( "SELECT m FROM movies m INNER JOIN m.producers p " +
            "WHERE p.id.firstName LIKE ?1 OR p.id.lastName LIKE ?1")
    Page<MovieEntity> findByProducersLike(String producer, Pageable pageable);

}
