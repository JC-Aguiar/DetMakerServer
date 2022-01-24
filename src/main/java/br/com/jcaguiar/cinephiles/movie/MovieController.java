package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("*/movie")
public class MovieController {

    @Autowired
    private MovieService service;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping(name = "/{genre}")
    public ResponseEntity<?> getGenre(@RequestParam(name = "genre") @Valid GenreEnum genre) {
        service.getMoviesByGenre(genre);
        return null;
    }

    @GetMapping(name = "/{example}")
    public ResponseEntity<?> getExampleOf(@RequestParam(name = "example") @Valid MoviePostRequest movie) {
        MovieEntity movieEntity = modelMapper.map(movie, MovieEntity.class);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
            .withIgnoreNullValues()
            .
        service.getMoviesByGenre(movieEntity);
        return null;
    }

    @GetMapping(name = "/{text}")
    public ResponseEntity<?> getText(@RequestParam(name = "text") String text) {
        service.
        return null;
    }
}
