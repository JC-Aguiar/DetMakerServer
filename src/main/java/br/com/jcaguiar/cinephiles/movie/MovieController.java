package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("*/movie")
public class MovieController {

    @Autowired
    private MovieService service;

    @Autowired
    private ModelMapper modelMapper;

    {
        System.out.println(this.getClass());
    }

    @GetMapping(name = "/{genre}")
    public ResponseEntity<?> getGenre
    (@RequestParam(name = "genre") @Valid GenreEnum genre) {
        service.getMoviesByGenre(genre);
        return null;
    }

    @GetMapping(name = "/{example}")
    public List<ResponseEntity<?>> getExampleOf
    (@RequestParam(name = "example") @Valid MoviePostRequest movie) {
        MovieEntity movieEntity = modelMapper.map(movie, MovieEntity.class);
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
            .withIgnoreNullValues()
            .withIgnoreCase();
        Example<MovieEntity> movieEx = Example.of(movieEntity, matcher);
        service.getMoviesByExample(movieEx);
        return null;
    }

    @GetMapping(name = "/{text}")
    public ResponseEntity<?> getText(@RequestParam(name = "text") String text) {
        return null;
    }
}
