package br.com.ppw.dma.movie;

import br.com.ppw.dma.company.ProducerEntity;
import br.com.ppw.dma.company.ProducerService;
import br.com.ppw.dma.enums.GenreEnum;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.util.Download;
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


    public Page<MovieEntity> getMoviesByGenre
    (@NotNull GenreEnum genre, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByGenres(genre, pageable));
    }


    public Page<MovieEntity> getMoviesByExample
    (@NotNull Example<MovieEntity> movieEx, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findAll(movieEx, pageable));
    }


    public Page<MovieEntity> getMoviesByTitle
    (@NotBlank String title, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByTitle(title, pageable));
    }


    public Page<MovieEntity> getMoviesBySynopsis
    (@NotBlank String synopsis, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findBySynopsis(synopsis, pageable));
    }


    public Page<MovieEntity> getMoviesByTextLike
    (@NotBlank String text, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByKeyword(text, pageable));
    }


    public Page<MovieEntity> getMoviesByActor
    (@NotBlank String actor, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByActorsLike(actor, pageable));
    }


    public Page<MovieEntity> getMoviesByDirector
    (@NotBlank String director, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByDirectorsLike(director, pageable));
    }


    public Page<MovieEntity> getMoviesByProducer
    (@NotBlank String producer, @NotNull Pageable pageable) {
        return proxy().pageCheck(dao.findByProducersLike(producer, pageable));
    }


    public Optional<MovieEntity> addOne(@NotNull MovieModel model) {
        final MovieEntity movie = (MovieEntity) model;
        return Optional.of(dao.save(movie));
    }


    public Optional<MovieEntity> addOne(@NotNull MovieEntity movie) {
        return Optional.of(dao.save(movie));
    }

    public Optional<MovieDtoTMDB> processTMDB(@NotNull Map<String, Object> file) {
        return parseMapToDto(filterMapTMDB(file));
    }

    public Optional<MovieDtoTMDB> processTMDB(@NotNull MultipartFile file) {
        return parseJsonToDto(parseFileToJson(file).get());
    }


    public Map<String, Object> filterMapTMDB(@NotNull Map<String, Object> file) {
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


    public Optional<MovieDtoTMDB> parseMapToDto(@NotNull Map<String, Object> file) {
//        final Instant startTime = Instant.now();
//        try {
            final String stringFile = gson.toJson(file);
            final MovieDtoTMDB dtoTMDB = gson.fromJson(stringFile, MovieDtoTMDB.class);
            return Optional.of(dtoTMDB);
//        } catch (JsonSyntaxException e) {
//            return Optional.empty();
//        }
    }




    @SneakyThrows
    public Optional<JsonObject> parseFileToJson(@NotNull MultipartFile file) {
        final String jsonString = new String(file.getBytes(), StandardCharsets.UTF_8);
        final JsonObject json = gson.fromJson(jsonString, JsonObject.class);
        return Optional.of(json);
//        final Instant startTime = Instant.now();
//        try {
//            final String jsonString = new String(file.getBytes(), StandardCharsets.UTF_8);
//            final JsonObject json = gson.fromJson(jsonString, JsonObject.class);
//            return MasterServiceLog.success(startTime, json);
//        } catch (IOException e) {
//            return MasterServiceLog.error(startTime, e);
//        }
    }


    public Optional<MovieDtoTMDB> parseJsonToDto(@NotNull JsonObject json) {
        return Optional.of(gson.fromJson(json, MovieDtoTMDB.class));
//        final Instant startTime = Instant.now();
//        try {
//            json.checkStatus();
//            final JsonObject jsonObj = json.compareAndGet(JsonObject.class);
//            return proxy().parseJsonToDto(jsonObj);
//            final MovieDtoTMDB movieDto = proxy().parseJsonToDto(jsonObj);
//            return MasterServiceLog.success(startTime, movieDto);
//        } catch (Exception e) {
//            return MasterServiceLog.error(startTime, e);
//        }
    }

//
//    //MovieDtoTMDB
//    private MovieDtoTMDB parseJsonToDto(@NotNull JsonObject json) {
//        return new Gson().fromJson(json, MovieDtoTMDB.class);
//    }


    @SneakyThrows
    public Optional<MovieEntity> persistDtoTMDB(@NotNull MovieDtoTMDB movieJson) {
          return    persistDtoTMDB(movieJson, true);
//        final Instant startTime = Instant.now();
//        try {
//            movieJson.checkStatus();
//            final MovieDtoTMDB movieDto = movieJson.compareAndGet(MovieDtoTMDB.class);
//            final MovieEntity movie = persistDtoTMDB(movieDto);
//            return MasterServiceLog.success(startTime, movie);
//        } catch (Exception e) {
//            return MasterServiceLog.error(startTime, e);
//        }
    }

    private Optional<MovieEntity> persistDtoTMDB(@NotNull MovieDtoTMDB movieJson, boolean ignore)
    throws ParseException, IOException {
        // Single attributes
        final String title = movieJson.getTitle();
        final String synopsis = movieJson.getOverview();
        final String tagline = movieJson.getTagline();
        final Date premier = new SimpleDateFormat("yyyy-MM-dd")
            .parse(movieJson.getRelease_date());
        final long runTime = Long.parseLong(movieJson.getRuntime());
        final Duration duration = Duration.ofMinutes(runTime);
        // Poster imagem from origin (URL + File)
        final String postersString =
            "https://image.tmdb.org/t/p/w600_and_h900_bestv2"
            + movieJson.getPoster_path();
        final byte[] poster = Download.from(postersString); //todo: link builder OK. But the download system is failing.
        // Poster
        final var teste = getPosterRepository();
        final PostersEntity postersEntity = teste.saveAndFlush(
            PostersEntity.builder()
                .url(postersString)
                .image(poster)
                .build());
        // Genres
        final List<String> possibleGenres = movieJson.getGenres()
            .stream()
            .map(MovieDtoTMDBGenre::getName)
            .toList();
        final List<GenreEntity> genres = possibleGenres.stream()
            .map(g -> getGenreService().loadOrSave(g)).toList();
        // Producers
        final List<String> possibleProducers = movieJson.getProduction_companies()
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
        return addOne(movie);
    }

}
