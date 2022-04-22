package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.company.CompanyEntity;
import br.com.jcaguiar.cinephiles.company.CompanyService;
import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import br.com.jcaguiar.cinephiles.util.Download;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

@Service
public class MovieService extends MasterService<Integer, MovieEntity, MovieService> {

    @Autowired
    private GenreService genreService;
    @Autowired
    private CompanyService companyService;
    @Autowired
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
//        add("backdrop_path");               //-> posters
        add("runtime");                     //-> duration
    }};

    public MovieService(MovieRepository dao) {
        super(dao);
        this.dao = dao;
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesByGenre(GenreEnum genre, Pageable pageable) {
        return proxy().pageCheck(dao.findByGenres(genre, pageable));
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx, Pageable pageable) {
        return proxy().pageCheck(dao.findAll(movieEx, pageable));
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesByTitle(String title, Pageable pageable) {
        return proxy().pageCheck(dao.findByTitle(title, pageable));
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesBySynopsis(String synopsis, Pageable pageable) {
        return proxy().pageCheck(dao.findBySynopsis(synopsis, pageable));
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesByTextLike(String text, Pageable pageable) {
        return proxy().pageCheck(dao.findByKeyword(text, pageable));
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesByActor(String actor, Pageable pageable) {
        return proxy().pageCheck(dao.findByActorsLike(actor, pageable));
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesByDirector(String director, Pageable pageable) {
        return proxy().pageCheck(dao.findByDirectorsLike(director, pageable));
    }

    @ConsoleLog
    public Page<MovieEntity> getMoviesByProducer(String producer, Pageable pageable) {
        return proxy().pageCheck(dao.findByProducersLike(producer, pageable));
    }

    //TODO: FINISH
    @ConsoleLog
    public MovieEntity addOne(MovieModel model) {
        final MovieEntity movie = (MovieEntity) model;
        return dao.save(movie);
    }

    @ConsoleLog
    public Map<String, Object> filterJsonTMDB(Map<String, Object> file) {
        // List containing the text values. THis removes unnecessary key/value from the given map/json
        final List<Object> values = TMDB_KEYS.stream().map(file::remove).toList();
        System.out.println("list of values: " + values.toString() );
        // The filtered map/json with only useful key/values.
        final Map<String, Object> moviesJson = new HashMap<>();
        TMDB_KEYS.forEach(k -> moviesJson.put(
            k, values.get(TMDB_KEYS.indexOf(k))));
        System.out.println("final map: ");
        moviesJson.forEach((k, v) -> System.out.println(k + ": " + v));
        return moviesJson;
    }

    public MovieEntity persistJsonTMDB(@NotNull MovieDtoTMDB movieJson) {
        try {
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
            //            final var connection = posterUrl.openConnection();
            //            final Object posterFile = connection.getContent();
            //            final var byteStream = new ByteArrayOutputStream();
            //            final var objectStream = new ObjectOutputStream(byteStream);
            final byte[] poster = Download.from(postersString);
            // Poster
            final PostersEntity postersEntity = posterRepository.saveAndFlush(
                PostersEntity.builder()
                    .url(postersString)
                    .image(poster)
                    .build());
            // Genres
            final List<String> possibleGenres = movieJson.getGenres()
                .stream()
                .map(MovieDtoTMDBGenre::getName)
                .toList();
            final List<GenreEntity> genres = possibleGenres.stream()  //todo: uncomment
                .map(genreService::loadOrSave).toList();  //todo: uncomment
            // Producers
            final List<String> possibleProducers = movieJson.getProduction_companies()
                .stream()
                .map(MovieDtoTMDBProductors::getName)
                .toList();
            final List<CompanyEntity> producers = possibleProducers.stream()  //todo: uncomment
                .map(companyService::loadOrSave).toList();  //todo: uncomment
            final List<PostersEntity> posters = new ArrayList<>();
            posters.add(postersEntity);
            final MovieEntity movie = MovieEntity.builder()
                .title(title)
                .synopsis(synopsis)
                .tagline(tagline)
                .premiereDate(premier)
                .duration(duration)
                .build();
            movie.addGenres(genres).addProducers(producers).addPosters(posters); //todo: uncomment
            final var teste01 = gson.toJson(movie);
            System.out.println(gson.fromJson(teste01, JsonObject.class).toString());
            return dao.saveAndFlush(movie);  //todo: uncomment
        } catch (ParseException | NumberFormatException | IOException e) {
            System.out.println("Persist TMDB dto error: " + e.getLocalizedMessage());
            return null;
        }
    }

    @ConsoleLog
    public JsonObject parseFileToJson(@NotNull MultipartFile file) {
        try {
            final String jsonString = new String(
                file.getBytes(), StandardCharsets.UTF_8);
            return gson.fromJson(jsonString, JsonObject.class);
        } catch (IOException e) {
            System.out.println("Parse MultipartFile to Json error: " + e.getLocalizedMessage());
            return gson.fromJson(" ", JsonObject.class);
        }
    }

    @ConsoleLog
    public MovieDtoTMDB parseJsonToDto(@NotNull JsonObject json) {
        try {
            return gson.fromJson(json, MovieDtoTMDB.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Parse Json to TMDB error: " + e.getLocalizedMessage());
            return new MovieDtoTMDB();
        }
    }

    //todo: remove this in production
    @ConsoleLog
    public void deleteAll() {
        dao.deleteAll();
    }

}
