package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Integer> {

    GenreEntity findByGenre(@NotNull GenreEnum genreEnum);
//    GenreEntity findByGenre(@NotBlank String genreEnum);

}
