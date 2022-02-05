package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@RestController
@RequestMapping("/movie")
public class MovieController {
    //ATTRIBUTES
    @Autowired
    private MovieService service;

    @Autowired
    private ModelMapper modelMapper;

    private final static ExampleMatcher MATCHER_ALL = ExampleMatcher
            .matchingAll().withIgnoreNullValues().withIgnoreCase();

    private final static ExampleMatcher MATCHER_ANY = ExampleMatcher
            .matchingAny().withIgnoreNullValues().withIgnoreCase();

    private static final Map<String, Method> ENDPOINTS_GET = new HashMap<>() {{
        try {
            put("genre", MovieController.class.getMethod("byGenre", String.class, int.class, int.class));
            put("title", MovieController.class.getMethod("byTitle", String.class, int.class, int.class));
            put("synopsis", MovieController.class.getMethod("bySynopsis", String.class, int.class, int.class));
            put("director", MovieController.class.getMethod("byDirector", String.class, int.class, int.class));
            put("actor", MovieController.class.getMethod("byActor", String.class, int.class, int.class));
            put("producer", MovieController.class.getMethod("byProducer", String.class, int.class, int.class));
            put("text", MovieController.class.getMethod("byText", String.class, int.class, int.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }};

    //GET: ALL MOVIES
    @ConsoleLog
    @GetMapping
    public ResponseEntity<?> all(@RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "itens", defaultValue = "12") int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getAll(pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET: MAP REQUEST PATH
    @ConsoleLog
    @GetMapping(path = "/{var}")
    public ResponseEntity<?> get(@PathVariable @NotBlank String var,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "itens", defaultValue = "12") int itens)
    throws InvocationTargetException, IllegalAccessException
    {
        var = var.toLowerCase(Locale.ROOT);
        System.out.println(String.format("[MOVIE] GET: page[%d] itens[%d]",page, itens));
        final Method methodCall = Optional.ofNullable(ENDPOINTS_GET.get(var))
            .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Incorrect path to '/movies' URL"));
        final Object[] params = new Object[] { var, page, itens };
        return (ResponseEntity<?>) methodCall.invoke(this, params);
    }

    //GET: by GENRE
    @ConsoleLog
    public ResponseEntity<?> byGenre(@NotBlank String genre, int page, int itens)
    {
        GenreEnum genreEnum = Arrays.stream(GenreEnum.values()).filter(
                en -> genre.equalsIgnoreCase(en.toString()))
                .findFirst().orElseThrow();
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByGenre(genreEnum, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET: by TITLE
    @ConsoleLog
    public ResponseEntity<Page> byTitle(@NotBlank String title, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByTitle(title, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET: by SYNOPSIS
    @ConsoleLog
    public ResponseEntity<Page> bySynopsis(@NotBlank String synopsis, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesBySynopsis(synopsis, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET: by DIRECTOR
    @ConsoleLog
    public ResponseEntity<Page> byDirector(@NotBlank String director, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByDirector(director, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET: by ACTOR
    @ConsoleLog
    public ResponseEntity<Page> byActor(@NotBlank String actor, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByActor(actor, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET: by PRODUCER
    @ConsoleLog
    public ResponseEntity<Page> byProducer(@NotBlank String producer, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByProducer(producer, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET: BY TITLE, SYNOPSIS, DIRECTOR, ACTOR or PRODUCER
    @ConsoleLog
    public ResponseEntity<?> byText(@NotBlank String text, int page, int itens)
    {
        final PageRequest pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final MovieEntity movieTemplate = MovieEntity.builder()
                .title(text)
                .synopsis(text)
                .build();
        movieTemplate.addDirector(text).addActor(text).addProctor(text);
        final Example<MovieEntity> movieEx = Example.of(movieTemplate, MATCHER_ANY);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //POST: ADVANCED SEARCH
    @ConsoleLog
    @PostMapping(name = "/example/{example}", params = {"page", "itens"})
    public ResponseEntity<Page> byExampleOf(@Valid MoviePostRequest movie, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final MovieEntity movieEntity = modelMapper.map(movie, MovieEntity.class);
        final Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageConfig);
        final Page<MovieDtoResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(m -> moviesResponse.toList().add(modelMapper.map(m, MovieDtoResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }
}
