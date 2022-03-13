package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.company.CompanyEntity;
import br.com.jcaguiar.cinephiles.company.CompanyService;
import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public MovieEntity persistJsonTMDB(Map<String, Object> moviesJson) {
        try {
            // Single attributes
            final String title = moviesJson.get("title").toString();
            final String synopsis = moviesJson.get("overview").toString();
            final String tagline = moviesJson.get("tagline").toString();
            final Date premier = new SimpleDateFormat("yyyy-MM-dd")
                .parse(moviesJson.get("release_date").toString());
            final long runTime = Long.parseLong(moviesJson.get("runtime").toString());
            final Duration duration = Duration.ofMinutes(runTime);
            // Poster imagem from origin (URL + File)
            final String postersString = "https://image.tmdb.org/t/p/w600_and_h900_bestv2"
                + moviesJson.get("poster_path").toString();
            final URL posterUrl = new URL(postersString);
            final File posterFile = FileUtils.toFile(posterUrl);
            final byte[] poster = FileUtils.readFileToByteArray(posterFile);
            final Path path = Paths.get("/temp");
            Files.write(path, poster);
            System.out.println("File saved in path: " + path);
            // Poster
            final PostersEntity postersEntity = posterRepository.saveAndFlush(
                PostersEntity.builder()
                    .url(postersString)
                    .image(poster)
                    .build());
            // Genres
            final List<String> possibleGenres = MapToStringTMDB(moviesJson, "genres");
            possibleGenres.removeIf(e -> e.contains("id"));
            final List<GenreEntity> genres = possibleGenres.stream()
                .map(genreService::loadOrSave).toList();
            // Producers
            final List<String> possibleProducers = MapToStringTMDB(moviesJson, "production_companies");
            possibleGenres.removeIf(e -> !e.contains("name"));
            final List<CompanyEntity> producers = possibleProducers.stream()
                .map(companyService::loadOrSave).toList();
            final List<PostersEntity> posters = new ArrayList<>();
            final MovieEntity movie = MovieEntity.builder()
                .title(title)
                .synopsis(synopsis)
                .tagline(tagline)
                .premiereDate(premier)
                .duration(duration)
                .build();
            movie.addGenres(genres).addProducers(producers).addPosters(posters);
            return dao.saveAndFlush(movie);
        } catch (ParseException | NumberFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // final HttpURLConnection connection = (HttpURLConnection) posterUrl.openConnection();
    // connection.setRequestMethod("GET");
    // connection.setConnectTimeout(5000);
    // connection.setReadTimeout(5000);
    // connection.connect();

    @ConsoleLog
    public Map parseFileToMap(MultipartFile file) {
        try {
            final String jsonString = new String(
                file.getBytes(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(jsonString, Map.class);
        } catch (IOException ignored) { }
        return new HashMap();
    }

    private List<String> MapToStringTMDB(Map json, String key) {
        return Arrays.asList(json.get(key).toString()
                      .replace("{", "").replace("}", "")
                      .replace("[", "").replace("]", "")
                      .split(","));
    }


}
