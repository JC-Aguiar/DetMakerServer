package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class GenreService {

    @Autowired
    GenreRepository repo;

    public GenreEntity loadOrSave(@NotBlank String name) {
        final GenreEnum genreEnum = checkEnum(name);
        return Optional.ofNullable(repo.findByGenre(genreEnum))
            .orElseGet(() -> newGenre(genreEnum));
    }

    private GenreEntity newGenre(@NotNull GenreEnum genreEnum) {
        final GenreEntity genreEntity = GenreEntity
            .builder().genre(genreEnum).build();
        return repo.saveAndFlush(genreEntity);
    }

    //TODO: mover isso para classe Enum
    private GenreEnum checkEnum(@NotBlank String GenreString) {
        System.out.println("Checking genre tag: " + GenreString);
        Arrays.stream(GenreEnum.values())
            .map(GenreEnum::getName)
            .forEach(g -> System.out.print(g + "  "));
        final GenreEnum genreEnum = Arrays.stream(GenreEnum.values())
              .filter(g -> g.getName().equalsIgnoreCase(GenreString))
              .findFirst()
              .orElseThrow();
        System.out.println("Genre detected: " + genreEnum);
        return genreEnum;
    }

//    private void checkgenreEnum(String name) {
//        Optional.ofNullable(
//            Arrays.stream(GenreEnum.values())
//                .filter(e -> e.toString().equalsIgnoreCase(name))
//        ).orElseThrow();
//    }

}
