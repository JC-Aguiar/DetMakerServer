package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@RestController
@RequestMapping("*/movie")
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
            put("text", MovieController.class.getMethod("byText", String.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }};

    //CLASS INIT
    {
        System.out.println(this.getClass().toString().toUpperCase());
    }

    //GET - MAP REQUEST PATH
    @GetMapping(name = "/{var}", params = {"page", "itens"})
    public ResponseEntity<?> get(@PathVariable String var,
                                  @RequestParam(required = false) int page,
                                  @RequestParam(required = false) int itens)
    throws InvocationTargetException, IllegalAccessException
    {
        itens = itens == 0 ? 12 : itens;
        var = var.toLowerCase(Locale.ROOT);
        final Method methodCall = Optional.ofNullable(ENDPOINTS_GET.get(var)).orElseThrow();
        final Object[] params = new Object[] { var, page, itens };
        return (ResponseEntity<?>) methodCall.invoke(this, params);
//        try {
//            Optional.of(ENDPOINTS.get(var)).ifPresentOrElse(
//                    method -> {
//                        List<Class<?>> classes = Arrays.asList(method.getParameterTypes());
//                        return (ResponseEntity<Page>) method.invoke(new MovieController(), classes);
//                    }, () -> {
//                        throw new RuntimeException("endereço de requisição não encontrado");
//                    });
//        } catch (IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//                throw new RuntimeException("erro no decorrer do processo");
//        }
    }

    //GET - FIND MOVIES BY GENRE
//    @GetMapping(name = "/{genre}", params = {"page", "itens"})
    public ResponseEntity<?> byGenre(@PathVariable @NotBlank String genre,
                                     @RequestParam(required = false) int page,
                                     @RequestParam(required = false, defaultValue = "12") int itens)
    {
        GenreEnum genreEnum = Arrays.stream(GenreEnum.values()).filter(
                en -> genre.equalsIgnoreCase(en.toString()))
                .findFirst().orElseThrow();
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByGenre(genreEnum, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieBasicResponse.class)));

        return null;
    }

    //GET - BY TITLE
//    @GetMapping(name = "/{title}", params = {"page", "itens"})
    public ResponseEntity<Page> byTitle(@PathVariable @NotBlank String title,
                                        @RequestParam(required = false) int page,
                                        @RequestParam(required = false, defaultValue = "12") int itens)
    {
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByTitle(title, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieBasicResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET - BY SYNOPSIS
//    @GetMapping(name = "/{synopsis}", params = {"page", "itens"})
    public ResponseEntity<Page> bySynopsis(@PathVariable @NotBlank String synopsis,
                                           @RequestParam(required = false) int page,
                                           @RequestParam(required = false, defaultValue = "12") int itens)
    {
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesBySynopsis(synopsis, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieBasicResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET - BY DIRECTOR
//    @GetMapping(name = "/{director}", params = {"page", "itens"})
    public ResponseEntity<Page> byDirector(@PathVariable @NotBlank String director,
                                        @RequestParam(required = false) int page,
                                        @RequestParam(required = false, defaultValue = "12") int itens) {
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByDirector(director, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieBasicResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET - BY ACTOR
//    @GetMapping(name = "/{director}", params = {"page", "itens"})
    public ResponseEntity<Page> byActor(@PathVariable @NotBlank String director,
                                        @RequestParam(required = false) int page,
                                        @RequestParam(required = false, defaultValue = "12") int itens)
    {
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByActor(director, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieBasicResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET - BY PRODUCER
//    @GetMapping(name = "/{producer}", params = {"page", "itens"})
    public ResponseEntity<Page> byProducer(@PathVariable @NotBlank String producer,
                                        @RequestParam(required = false) int page,
                                        @RequestParam(required = false, defaultValue = "12") int itens)
    {
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByProducer(producer, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(movie -> moviesResponse.toList().add(modelMapper.map(movie, MovieBasicResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }

    //GET - BY TITLE, SYNOPSIS, DIRECTOR, ACTOR, PRODUCER
//    @GetMapping(name = "/{text}")
    public ResponseEntity<?> byText(@PathVariable String text) {
        return null;
    }

    //POST - BY ADVANCED SEARCH
    @PostMapping(name = "/{example}", params = {"page", "itens"})
    public ResponseEntity<Page> byExampleOf(@PathVariable @Valid MoviePostRequest movie,
                                            @RequestParam(required = false) int page,
                                            @RequestParam(required = false, defaultValue = "12") int itens   )
    {
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final MovieEntity movieEntity = modelMapper.map(movie, MovieEntity.class);
        final Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(m -> moviesResponse.toList().add(modelMapper.map(m, MovieBasicResponse.class)));
        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
    }
}
