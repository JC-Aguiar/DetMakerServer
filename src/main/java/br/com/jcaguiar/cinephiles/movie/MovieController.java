package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterPagination;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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

    //FIND MOVIES BY GENRE
    @GetMapping(name = "/{genre}")
    public ResponseEntity<?> getGenre (@RequestParam(name = "genre") @Valid GenreEnum genre) {
        service.getMoviesByGenre(genre);
        return null;
    }

    //FIND MOVIES BY TITLE
    @GetMapping(name = "/{title}")
    public ResponseEntity<?> getTitle (@RequestParam(name = "title") @NotBlank String title) {
        service.getMoviesByTitle(title);
        return null;
    }

    //FIND MOVIES BY ADVANCED SEARCH
    @GetMapping(name = "/{example}")
    public ResponseEntity<Pageable> getExampleOf (@RequestParam(name = "example") @Valid MoviePostRequest movie) {
        MovieEntity movieEntity = modelMapper.map(movie, MovieEntity.class);
        Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx);
        Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(m -> {
            moviesResponse.toList().add(modelMapper.map(m, MovieBasicResponse.class));
        });
        Pageable moviesPage = PageRequest.of(0, moviesResponse.getTotalPages(), Sort.Direction.ASC);
        return new ResponseEntity<Pageable>(
                (Pageable) MasterPagination.pageResult(moviesResponse),
                HttpStatus.OK);
    }

    //FIND MOVIES BY SEARCHING WITHIN ALL STRING PROPERTIES
    @GetMapping(name = "/{text}")
    public ResponseEntity<?> getText(@RequestParam(name = "text") String text) {
        return null;
    }
}
