package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("*/movie")
public class MovieController {

    @Autowired
    private MovieService service;

    @GetMapping(name = "/{genre}")
    public ResponseStatus<?> getGenre(@RequestParam(name = "genre") @Valid GenreEnum genre) {
        service.
        return null;
    }
}
