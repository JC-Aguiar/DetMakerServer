package br.com.jcaguiar.cinephiles.movie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Integer> {

    GenreEntity findByGenreName(@NotBlank String genreName);

}
