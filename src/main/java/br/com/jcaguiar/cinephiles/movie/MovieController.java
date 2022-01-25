package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterPagination;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("*/movie")
public class MovieController {

    @Autowired
    private MovieService service;

    @Autowired
    private ModelMapper modelMapper;

    private final static ExampleMatcher MATCHER_ALL = ExampleMatcher
            .matchingAll()
            .withIgnoreNullValues()
            .withIgnoreCase();

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
    public ResponseEntity<Pageable> getExampleOf
    (@RequestParam(name = "example") @Valid MoviePostRequest movie) {
        MovieEntity movieEntity = modelMapper.map(movie, MovieEntity.class);
        Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        List<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx);
        List<MovieBasicResponse> moviesResponse = new ArrayList<>();
        moviesEntities.forEach(m -> {
            moviesResponse.add(modelMapper.map(m, MovieBasicResponse.class));
        });
        return new ResponseEntity<Pageable>(
                (Pageable) MasterPagination.pageResult(moviesResponse),
                HttpStatus.OK);
    }

    @GetMapping(name = "/{text}")
    public ResponseEntity<?> getText(@RequestParam(name = "text") String text) {
        return null;
    }
}
