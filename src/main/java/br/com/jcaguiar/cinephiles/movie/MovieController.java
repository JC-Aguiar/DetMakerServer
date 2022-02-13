package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterController;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;

@RestController
@RequestMapping("/movie")
public class MovieController extends MasterController<Integer, MovieEntity, MovieDtoRequest, MovieDtoResponse> {

    private MovieService service;

//    private final Map<String, Method> endpointsGet = new HashMap<>() {{
//        try {
//            put("genre", MovieController.class.getMethod("byGenre", String.class, int.class, int.class));
//            put("title", MovieController.class.getMethod("byTitle", String.class, int.class, int.class));
//            put("synopsis", MovieController.class.getMethod("bySynopsis", String.class, int.class, int.class));
//            put("director", MovieController.class.getMethod("byDirector", String.class, int.class, int.class));
//            put("actor", MovieController.class.getMethod("byActor", String.class, int.class, int.class));
//            put("producer", MovieController.class.getMethod("byProducer", String.class, int.class, int.class));
//            put("text", MovieController.class.getMethod("byText", String.class, int.class, int.class));
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//    }};

    public MovieController(MovieService service) {
        super(MovieController.class, service);
        try {
            ENDPOINTS_GET.put("genre", getClass().getMethod("byGenre", String.class, int.class, int.class));
            ENDPOINTS_GET.put("title", getClass().getMethod("byTitle", String.class, int.class, int.class));
            ENDPOINTS_GET.put("synopsis", getClass().getMethod("bySynopsis", String.class, int.class, int.class));
            ENDPOINTS_GET.put("director", getClass().getMethod("byDirector", String.class, int.class, int.class));
            ENDPOINTS_GET.put("actor", getClass().getMethod("byActor", String.class, int.class, int.class));
            ENDPOINTS_GET.put("producer", getClass().getMethod("byProducer", String.class, int.class, int.class));
            ENDPOINTS_GET.put("text", getClass().getMethod("byText", String.class, int.class, int.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        printInfo();
    }

    //GET: ALL MOVIES
//    @ConsoleLog
//    @GetMapping
//    public ResponseEntity<?> all(@RequestParam(name = "page", defaultValue = "0") int page,
//                                 @RequestParam(name = "itens", defaultValue = "12") int itens)
//    {
//        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
//        final Page<MovieEntity> moviesEntities = service.findAll(pageConfig);
//        final Page<MovieDtoResponse> moviesResponse = moviesEntities.map(this::parseToResponseDto);
//        return new ResponseEntity<>(moviesResponse, HttpStatus.OK);
//    }

    //GET: MAP REQUEST PATH
//    @ConsoleLog
//    @GetMapping(path = "/{var}")
//    public ResponseEntity<?> get(@PathVariable @NotBlank String var,
//                                 @RequestParam(name = "page", defaultValue = "0") int page,
//                                 @RequestParam(name = "itens", defaultValue = "12") int itens)
//    throws InvocationTargetException, IllegalAccessException
//    {
//        var = var.toLowerCase(Locale.ROOT);
//        final Method methodCall = Optional.ofNullable(ENDPOINTS_GET.get(var))
//            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incorrect path to '/movies' URL"));
//        final Object[] params = new Object[] { var, page, itens };
//        return (ResponseEntity<?>) methodCall.invoke(this, params);
//    }

    //GET: by GENRE
    @ConsoleLog
    public ResponseEntity<?> byGenre(@NotBlank String genre, int page, int itens)
    {
        GenreEnum genreEnum = Arrays.stream(GenreEnum.values()).filter(
                en -> genre.equalsIgnoreCase(en.toString()))
                .findFirst().orElseThrow();
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByGenre(genreEnum, pageConfig);
        return craftResponsePage(moviesEntities);
    }

    //GET: by TITLE
    @ConsoleLog
    public ResponseEntity<?> byTitle(@NotBlank String title, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByTitle(title, pageConfig);
        return craftResponsePage(moviesEntities);
    }

    //GET: by SYNOPSIS
    @ConsoleLog
    public ResponseEntity<?> bySynopsis(@NotBlank String synopsis, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesBySynopsis(synopsis, pageConfig);
        return craftResponsePage(moviesEntities);
    }

    //GET: by DIRECTOR
    @ConsoleLog
    public ResponseEntity<?> byDirector(@NotBlank String director, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByDirector(director, pageConfig);
        return craftResponsePage(moviesEntities);
    }

    //GET: by ACTOR
    @ConsoleLog
    public ResponseEntity<?> byActor(@NotBlank String actor, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByActor(actor, pageConfig);
        return craftResponsePage(moviesEntities);
    }

    //GET: by PRODUCER
    @ConsoleLog
    public ResponseEntity<?> byProducer(@NotBlank String producer, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByProducer(producer, pageConfig);
        return craftResponsePage(moviesEntities);
    }

    //GET: by KEY-WORD
    @ConsoleLog
    public ResponseEntity<?> byText(@NotBlank String text, int page, int itens)
    {
        final PageRequest pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final MovieEntity movieTemplate = MovieEntity.builder()
                .title(text).synopsis(text).build();
        movieTemplate.addDirector(text).addActor(text).addProctor(text);
        final Example<MovieEntity> movieEx = Example.of(movieTemplate, MATCHER_ANY);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageConfig);
        return craftResponsePage(moviesEntities);
    }

    //POST: ADVANCED SEARCH
    @ConsoleLog
    @PostMapping(name = "/example/{example}", params = {"page", "itens"})
    public ResponseEntity<?> byExampleOf(@Valid MovieDtoRequest movie, int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final MovieEntity movieEntity = parseToEntity(movie);
        final Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageConfig);
        return craftResponsePage(moviesEntities);
    }
}
