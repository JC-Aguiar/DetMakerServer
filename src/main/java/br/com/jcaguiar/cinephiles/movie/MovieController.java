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
