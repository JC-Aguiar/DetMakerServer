package br.com.jcaguiar.cinephiles.movie;

import br.com.jcaguiar.cinephiles.enums.GenreEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.spel.spi.Function;
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

    private static final Map<String, Method> ENDPOINTS = new HashMap<String, Method>() {{
        try {
            put("genre", MovieController.class.getMethod("getGenre", GenreEnum.class));
            put("title", MovieController.class.getMethod("getTitle", String.class));
            put("example", MovieController.class.getMethod("getExampleOf", MoviePostRequest.class, int.class, int.class));
            put("text", MovieController.class.getMethod("getText", String.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }};

    //CLASS INIT
    {
        System.out.println(this.getClass().toString().toUpperCase());
    }

    //FIND MOVIES BY GENRE
    @GetMapping(name = "/{var}", params = {"page", "itens"})
    public ResponseEntity<?> get(@PathVariable String var,
                                    @RequestParam(required = false) int page,
                                    @RequestParam(required = false) int itens)
    throws InvocationTargetException, IllegalAccessException
    {
        Method methodCall = Arrays.stream(MovieController.class.getMethods())
                .filter(method -> method.getName().equalsIgnoreCase(var))
                .findFirst().orElseThrow();
        itens = itens == 0 ? 12 : itens;
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

    //FIND MOVIES BY GENRE
    @GetMapping(name = "/{genre}", params = {"page", "itens"})
    public ResponseEntity<?> getGenre (@PathVariable @Valid String genre)
    {
        GenreEnum genreEnum = Arrays.stream(GenreEnum.values()).filter(
                en -> genre.equalsIgnoreCase(en.toString())).findFirst().orElseThrow();
        service.getMoviesByGenre(genreEnum);
        return null;
    }

    //FIND MOVIES BY TITLE
    @GetMapping(name = "/{title}", params = {"page", "itens"})
    public ResponseEntity<?> getTitle (@PathVariable @NotBlank String title) {
        service.getMoviesByTitle(title);
        return null;
    }

    //FIND MOVIES BY ADVANCED SEARCH
    @GetMapping(name = "/{example}", params = {"page", "itens"})
    public ResponseEntity<Page> getExampleOf (@PathVariable @Valid MoviePostRequest movie,
                                              @RequestParam(required = false) int page,
                                              @RequestParam(required = false) int itens   )
    {
        itens = itens == 0 ? 12 : itens;
        final Pageable pageResult = PageRequest.of(page, itens, Sort.by("title").ascending());
        final MovieEntity movieEntity = modelMapper.map(movie, MovieEntity.class);
        final Example<MovieEntity> movieEx = Example.of(movieEntity, MATCHER_ALL);
        final Page<MovieEntity> moviesEntities = service.getMoviesByExample(movieEx, pageResult);
        final Page<MovieBasicResponse> moviesResponse = Page.empty();
        moviesEntities.forEach(m -> moviesResponse.toList().add(modelMapper.map(m, MovieBasicResponse.class)));
        return new ResponseEntity<Page>(moviesResponse, HttpStatus.OK);
    }

    //FIND MOVIES BY SEARCHING WITHIN ALL STRING PROPERTIES
    @GetMapping(name = "/{text}")
    public ResponseEntity<?> getText(@PathVariable String text) {
        return null;
    }
}
