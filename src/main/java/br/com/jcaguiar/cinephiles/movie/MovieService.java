package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import br.com.jcaguiar.cinephiles.master.MasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieService extends MasterService<Integer, MovieEntity, MovieService> {

    private final MovieRepository dao;
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

    public MovieService(MovieRepository dao) {
        super(dao);
        this.dao = dao;
    }

    public Page<MovieEntity> getMoviesByGenre(GenreEnum genre, Pageable pageable) {
        return proxy().pageCheck(dao.findByGenres(genre, pageable));
    }

    public Page<MovieEntity> getMoviesByExample(Example<MovieEntity> movieEx, Pageable pageable) {
        return proxy().pageCheck(dao.findAll(movieEx, pageable));
    }

    public Page<MovieEntity> getMoviesByTitle(String title, Pageable pageable) {
        return proxy().pageCheck(dao.findByTitle(title, pageable));
    }

    public Page<MovieEntity> getMoviesBySynopsis(String synopsis, Pageable pageable) {
        return proxy().pageCheck(dao.findBySynopsis(synopsis, pageable));
    }

    public Page<MovieEntity> getMoviesByTextLike(String text, Pageable pageable) {
        return proxy().pageCheck(dao.findByKeyword(text, pageable));
    }

    public Page<MovieEntity> getMoviesByActor(String actor, Pageable pageable) {
        return proxy().pageCheck(dao.findByActorsLike(actor, pageable));
    }

    public Page<MovieEntity> getMoviesByDirector(String director, Pageable pageable) {
        return proxy().pageCheck(dao.findByDirectorsLike(director, pageable));
    }

    public Page<MovieEntity> getMoviesByProducer(String producer, Pageable pageable) {
        return proxy().pageCheck(dao.findByProducersLike(producer, pageable));
    }

    public MovieEntity addOne(MovieModel model) {
        final MovieEntity movie = (MovieEntity) model;
        return dao.save(movie);
    }

    public Map<String, Object> craftMovieJson(Map<String, Object> file) {
        final List<Object> values = KEYS.stream().map(file::remove).toList();
        final Map<String, Object> moviesJson = new HashMap<>();
        KEYS.forEach(k -> moviesJson.put(
            k, values.get(KEYS.indexOf(k))));
        //  moviesJson.forEach((k, v) -> System.out.println(k + ": " + v));
        return moviesJson;
    }

    public Map parseFileToMap(MultipartFile file) {
        try {
            final String jsonString = new String(
                file.getBytes(), StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(jsonString, Map.class);
        } catch (IOException ignored) { }
        return new HashMap();
    }

}
