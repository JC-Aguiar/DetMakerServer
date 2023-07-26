package br.com.ppw.dma.movie;

import br.com.ppw.dma.enums.GenreEnum;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterProcessLog;
import br.com.ppw.dma.master.MasterControllerResult;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("movie")
public class MovieController extends MasterController
    <Integer, MovieEntity, MovieDtoRequest, MovieDtoResponse, MovieController> {

    private final MovieService service;

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
    public ResponseEntity<?> byTitle(@NotBlank String title, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByTitle(title, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by SYNOPSIS
    // A shortcut to create a ResponseEntity with the status CREATED.
    public ResponseEntity<?> bySynopsis(@NotBlank String synopsis, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesBySynopsis(synopsis, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by DIRECTOR
    // A shortcut to create a ResponseEntity with the status CREATED.
    public ResponseEntity<?> byDirector(@NotBlank String director, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByDirector(director, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by ACTOR
    // A shortcut to create a ResponseEntity with the status CREATED.
    public ResponseEntity<?> byActor(@NotBlank String actor, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByActor(actor, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by PRODUCER
    // A shortcut to create a ResponseEntity with the status CREATED.
    public ResponseEntity<?> byProducer(@NotBlank String producer, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final Page<MovieEntity> moviesEntities = service.getMoviesByProducer(producer, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    //GET: by KEY-WORD
    // A shortcut to create a ResponseEntity with the status CREATED.
    public ResponseEntity<?> byText(@NotBlank String text, int page, int itens) {
        final PageRequest pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        //        final MovieEntity movieTemplate = MovieEntity.builder()
        //                .title(text).synopsis(text).build();
        //        movieTemplate.addDirector(text).addActor(text).addProctor(text);
        //        final Example<MovieEntity> movieEx = Example.of(movieTemplate, MATCHER_ANY);
        final Page<MovieEntity> moviesEntities = service.getMoviesByTextLike(text, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    /** POST: ADVANCED SEARCH <br>
     * This function unify different movie's attributes to find whatever fits all this conditions. The system will only
     * compare with all database and if all the requested attributes match will be added as an acceptable result.
     *
     * @param movie The movie object that will be used as an example to search for movies.
     * @param page The page number to be returned.
     * @param itens the number of items per page
     * @return A {@link ResponseEntity} with a {@link MasterControllerResult} that contains: a {@link Page} of
     * {@link MovieEntity} and a simple log;
     */
    @PostMapping(name = "search", params = {"page", "itens"})
    public ResponseEntity<?> byExampleOf(@Valid MovieDtoRequest movie, int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());

        //TODO: move this actions to service layer and apply here the ServiceProcess protocol !!!

        final MovieEntity movieEntity = parseToEntity(movie);
        final Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageConfig);
        return proxy().craftResponsePage(moviesEntities);
    }

    /** POST: INSERT ONE FILE <br>
     * Endpoint for JSON objects with THE MOVIE DATA BASE structure.
     * It will parse this JSON into a DTO, then persists it.
     *
     * @param file The JSON to be processed.
     * @return A {@link ResponseEntity} with a {@link MasterControllerResult} that contains: a {@link Page} of
     * {@link MovieEntity} and a simple log;
     */
    @PostMapping(value = "add/one/tmdb", consumes = {"application/json", "text/plain"})
    public ResponseEntity<?> addOne(@RequestBody final Map<String, Object> file) {
        final Pageable pageConfig = PageRequest.of(0, 1, Sort.by("title").ascending());
        final MovieDtoTMDB dto = service.processTMDB(file).get();
        return new ResponseEntity<>(service.persistDtoTMDB(dto), HttpStatus.OK);
//        return proxy().craftResponsePage(service.persistDtoTMDB(dto), pageConfig);
    }

    /** POST: INSERT MANY FILES <br>
     * Endpoint to insert many JSON objects with THE MOVIE DATA BASE structure.
     * Because the system already excepts a big amount of files, they will come as a list of {@link MultipartFile}.
     * Everything in the list it will be processed separately.
     * First, it will parse to JSON, then parse to DTO, and then it will be persisted.
     *
     * @param files The list of files to be processed.
     * @param page The page number to return.
     * @param itens The number of items per page.
     * @return A {@link ResponseEntity} with a {@link MasterControllerResult} that contains: a {@link Page} of
     * {@link MovieEntity} and a simple log;
     */
    @PostMapping(value = "add/many/tmdb", consumes = "multipart/form-data")
    public ResponseEntity<?> addAll(
        @RequestParam("files") final List<MultipartFile> files,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("title").ascending());
        final List<Optional<MovieEntity>> movies = files.stream()
            .map(service::processTMDB)
            .map(Optional::get)
            .map(service::persistDtoTMDB)
            .toList();
        return new ResponseEntity<>(movies, HttpStatus.OK); //proxy().craftResponse(movies, pageConfig);
    }

    //todo: remove this in production
    /** DELETE: ALL FILES <br>
     * Delete all the movie records in the database
     *
     * @return A {@link ResponseEntity} with a {@link MasterProcessLog} that contains a message and a simple log;
     */
    @DeleteMapping("del/all")
    public ResponseEntity<?> deleteAll() {
        service.deleteAll();
        return new ResponseEntity<>("All movies have been successfully deleted", HttpStatus.OK);
    }

}
