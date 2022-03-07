package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterController;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("movie")
public class MovieController extends MasterController
    <Integer, MovieEntity, MovieDtoRequest, MovieDtoResponse, MovieController> {

    private final MovieService service;
    private final static List<String> KEYS = new ArrayList<String>(){{
        add("backdrop_path");
        add("genres");
        add("original_title");
        add("overview");
        add("poster_path");
        add("production_companies");
        add("release_date");
        add("runtime");
        add("tagline");
        add("title");
    }};

    public MovieController(MovieService service) {
        super(service);
        this.service = service;
        try {
            endpointsGet.put("genre", getClass().getMethod("byGenre", String.class, int.class, int.class));
            endpointsGet.put("title", getClass().getMethod("byTitle", String.class, int.class, int.class));
            endpointsGet.put("synopsis", getClass().getMethod("bySynopsis", String.class, int.class, int.class));
            endpointsGet.put("director", getClass().getMethod("byDirector", String.class, int.class, int.class));
            endpointsGet.put("actor", getClass().getMethod("byActor", String.class, int.class, int.class));
            endpointsGet.put("producer", getClass().getMethod("byProducer", String.class, int.class, int.class));
            endpointsGet.put("text", getClass().getMethod("byText", String.class, int.class, int.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        printInfo();
    }

    //GET: by GENRE
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    public ResponseEntity<?> byGenre(@NotBlank String genre, int page, int itens) {
        GenreEnum genreEnum = Arrays.stream(GenreEnum.values())
                                    .filter(en -> genre.equalsIgnoreCase(en.toString()))
                                    .findFirst()
                                    .orElseThrow();
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByGenre(genreEnum, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by TITLE
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    public ResponseEntity<?> byTitle(@NotBlank String title, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByTitle(title, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by SYNOPSIS
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    public ResponseEntity<?> bySynopsis(@NotBlank String synopsis, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesBySynopsis(synopsis, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by DIRECTOR
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    public ResponseEntity<?> byDirector(@NotBlank String director, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByDirector(director, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by ACTOR
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    public ResponseEntity<?> byActor(@NotBlank String actor, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByActor(actor, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by PRODUCER
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    public ResponseEntity<?> byProducer(@NotBlank String producer, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByProducer(producer, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by KEY-WORD
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    public ResponseEntity<?> byText(@NotBlank String text, int page, int itens) {
        final PageRequest pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        //        final MovieEntity movieTemplate = MovieEntity.builder()
        //                .title(text).synopsis(text).build();
        //        movieTemplate.addDirector(text).addActor(text).addProctor(text);
        //        final Example<MovieEntity> movieEx = Example.of(movieTemplate, MATCHER_ANY);
        final Page<MovieEntity> moviesEntities = service.getMoviesByTextLike(text, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //POST: ADVANCED SEARCH
    // A shortcut to create a ResponseEntity with the status CREATED.
    @ConsoleLog
    @PostMapping(name = "search", params = {"page", "itens"})
    public ResponseEntity<?> byExampleOf(@Valid MovieDtoRequest movie, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final MovieEntity movieEntity = parseToEntity(movie);
        final Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //POST: INSERT ONE
    @ConsoleLog
    @PostMapping(value = "add/one/tmdb", consumes = "application/json")
    public ResponseEntity<?> addOne(final @RequestBody Map<String, Object> file) {
        final List<Object> values = KEYS.stream().map(file::remove).toList();
        final Map<String, Object> moviesJson = new HashMap<>();
        KEYS.forEach(k -> moviesJson.put(
            k, values.get(KEYS.indexOf(k))));
        moviesJson.forEach((k, v) -> System.out.println(k + ": " + v));
        return new ResponseEntity(null, HttpStatus.OK);
    }

    //POST: INSERT ONE
    @ConsoleLog
    @PostMapping(value = "add/more/tmdb", consumes = "multipart/form-data")
    public ResponseEntity<?> addAll(@RequestParam("files") List<MultipartFile> files)
    throws IOException {
        final Map jsonMap = new HashMap();
        for (MultipartFile file : files) {
            final String jsonString =  new String(file.getBytes(), StandardCharsets.UTF_8);
            jsonMap.putAll(new ObjectMapper().readValue(
                jsonString, Map.class)); }
        //TODO: falta ajustar ainda
        return proxy().addOne(jsonMap);
    }

}
