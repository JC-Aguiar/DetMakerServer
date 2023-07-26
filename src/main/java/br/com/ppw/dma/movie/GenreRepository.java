package br.com.ppw.dma.movie;

import br.com.ppw.dma.enums.GenreEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;

@Repository
public interface GenreRepository extends JpaRepository<GenreEntity, Integer> {

    GenreEntity findByGenre(@NotNull GenreEnum genreEnum);
//    GenreEntity findByGenre(@NotBlank String genreEnum);

}
