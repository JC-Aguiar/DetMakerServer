package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
public class GenreService extends MasterService<Integer, GenreEntity, GenreService> {

    private GenreRepository dao;

    public GenreService(GenreRepository dao) {
        super(dao);
        this.dao = dao;
    }

    @ConsoleLog
    public GenreEntity loadOrSave(@NotBlank String name) {
        final GenreEnum genreEnum = GenreEnum.checkEnum(name);
        return Optional.ofNullable(dao.findByGenre(genreEnum))
            .orElseGet(() -> newGenre(genreEnum));
    }

    @ConsoleLog
    private GenreEntity newGenre(@NotNull GenreEnum genreEnum) {
        final GenreEntity genreEntity = GenreEntity
            .builder().genre(genreEnum).build();
        return dao.saveAndFlush(genreEntity);
    }

}
