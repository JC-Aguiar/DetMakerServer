package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Service
public class GenreService {

    @Autowired
    GenreRepository repo;

    public GenreEntity loadOrSave(@NotBlank String name) {
        return Optional.ofNullable(repo.findByGenreName(name))
            .orElseGet(() -> newGenre(name));
    }

    private GenreEntity newGenre(@NotBlank String name) {
        final GenreEnum genreEnum = GenreEnum.valueOf(name);
        final GenreEntity genreEntity = GenreEntity
            .builder().genre(genreEnum).build();
        return repo.saveAndFlush(genreEntity);
    }

//    private void checkgenreEnum(String name) {
//        Optional.ofNullable(
//            Arrays.stream(GenreEnum.values())
//                .filter(e -> e.toString().equalsIgnoreCase(name))
//        ).orElseThrow();
//    }

}
