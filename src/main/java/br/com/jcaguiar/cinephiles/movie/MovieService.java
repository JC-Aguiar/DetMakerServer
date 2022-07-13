package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.company.ProducerEntity;
import br.com.jcaguiar.cinephiles.company.ProducerService;
import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import br.com.jcaguiar.cinephiles.master.ProcessLine;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import br.com.jcaguiar.cinephiles.util.Download;
import br.com.jcaguiar.cinephiles.util.ServiceProcess;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class MovieService extends MasterService<Integer, MovieEntity, MovieService> {

    @Autowired @Getter
    private GenreService genreService;
    @Autowired @Getter
    private ProducerService producerService;
//    @Autowired
    @Getter
    private PostersRepository posterRepository;
    @Autowired
    private Gson gson;
    private final MovieRepository dao;
    private final static List<String> TMDB_KEYS = new ArrayList<String>(){{
        add("title");                       //-> title
        add("overview");                    //-> synopsis
        add("tagline");                     //-> tagline
        add("release_date");                //-> premiereDate
        add("genres");                      //-> genres
        add("production_companies");        //-> producers
        add("poster_path");                 //-> posters
        //add("backdrop_path");               //-> posters
        add("runtime");                     //-> duration
    }};

    public MovieService(MovieRepository dao, PostersRepository posterRepository) {
        super(dao);
        this.dao = dao;
        setPostersRepository(posterRepository);
    }

    @Autowired
    public void setPostersRepository(PostersRepository postersRepository) {
        this.posterRepository = postersRepository;
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesByGenre
    (@NotNull GenreEnum genre, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByGenres(genre, pageable));
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesByExample
    (@NotNull Example<MovieEntity> movieEx, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findAll(movieEx, pageable));
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesByTitle
    (@NotBlank String title, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByTitle(title, pageable));
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesBySynopsis
    (@NotBlank String synopsis, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findBySynopsis(synopsis, pageable));
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesByTextLike
    (@NotBlank String text, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByKeyword(text, pageable));
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesByActor
    (@NotBlank String actor, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByActorsLike(actor, pageable));
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesByDirector
    (@NotBlank String director, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByDirectorsLike(director, pageable));
    }

    @ServiceProcess
    public Page<MovieEntity> getMoviesByProducer
    (@NotBlank String producer, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByProducersLike(producer, pageable));
    }

    //TODO: FINISH
    @ServiceProcess
    public MovieEntity addOne(@NotNull MovieModel model) {
        final MovieEntity movie = (MovieEntity) model;
        return dao.save(movie);
    }

    @ServiceProcess
    public Map<String, Object> filterJsonTMDB(@NotNull Map<String, Object> file) {
        // List containing the text values. This removes unnecessary key/value from the given map/json
        final List<Object> values = TMDB_KEYS.stream().map(file::remove).toList();
        // The filtered map/json with only useful key/values.
        final Map<String, Object> moviesJson = new HashMap<>();
        TMDB_KEYS.forEach(k -> moviesJson.put(
            k,
            values.get(TMDB_KEYS.indexOf(k))
        ));
        return moviesJson;
    }

    @ServiceProcess
    public MovieDtoTMDB parseMapToDto(@NotNull Map<String, Object> file) {
        final String stringFile = gson.toJson(file);
        return gson.fromJson(stringFile, MovieDtoTMDB.class);
    }

    @ServiceProcess
    @SneakyThrows
    public JsonObject parseFileToJson(@NotNull MultipartFile file) {
        final String jsonString = new String(file.getBytes(), StandardCharsets.UTF_8);
        return gson.fromJson(jsonString, JsonObject.class);
    }

    @ServiceProcess
    public MovieDtoTMDB parseJsonToDto(@NotNull JsonObject json) {
        return new Gson().fromJson(json, MovieDtoTMDB.class); //TODO: new Gson its really need here?
    }

    @ServiceProcess
    @SneakyThrows
    public MovieEntity persistDtoTMDB(@NotNull MovieDtoTMDB movieDto) {
        // Single attributes
        final String title = movieDto.getTitle();
        final String synopsis = movieDto.getOverview();
        final String tagline = movieDto.getTagline();
        final Date premier = new SimpleDateFormat("yyyy-MM-dd")
            .parse(movieDto.getRelease_date());
        final long runTime = Long.parseLong(movieDto.getRuntime());
        final Duration duration = Duration.ofMinutes(runTime);
        // Poster imagem from origin (URL + File)
        final String postersString =
            "https://image.tmdb.org/t/p/w600_and_h900_bestv2"
            + movieDto.getPoster_path();
        final byte[] poster = Download.from(postersString); //todo: link builder OK. But the download system is failing.
        // Poster
        final var teste = getPosterRepository();
        final PostersEntity postersEntity = teste.saveAndFlush(
            PostersEntity.builder()
                .url(postersString)
                .image(poster)
                .build());
        // Genres
        final List<String> possibleGenres = movieDto.getGenres()
            .stream()
            .map(MovieDtoTMDBGenre::getName)
            .toList();
        final List<GenreEntity> genres = possibleGenres.stream()
            .map(g -> getGenreService().loadOrSave(g)).toList();
        // Producers
        final List<String> possibleProducers = movieDto.getProduction_companies()
            .stream()
            .map(MovieDtoTMDBProductors::getName)
            .toList();
        final List<ProducerEntity> producers = possibleProducers.stream()
            .map(producerService::loadOrSave).toList();
        final List<PostersEntity> posters = List.of(postersEntity);
        final MovieEntity movie = MovieEntity.builder()
            .title(title)
            .synopsis(synopsis)
            .tagline(tagline)
            .premiereDate(premier)
            .duration(duration)
            .build();
        movie.addGenres(genres).addProducers(producers).addPosters(posters); //TODO: this need bee done in a service level?
        //TODO: posters aren't coming with movies_id relationship
        return dao.saveAndFlush(movie);
    }

    //todo: remove this in production
    @ServiceProcess
    public void deleteAll() {
        dao.deleteAll();
    }

}
