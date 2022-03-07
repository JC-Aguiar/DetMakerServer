package br.com.jcaguiar.cinephiles.master;

import br.com.jcaguiar.cinephiles.util.ConsoleLog;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
public abstract class MasterController<
    ID, ENTITY extends MasterEntity, REQUEST extends MasterDtoRequest,
    RESPONSE extends MasterDtoResponse, CTRL extends MasterController> {

    @Autowired
    private ModelMapper modelMapper;
    @Getter
    private final MasterService<ID, ENTITY> service;
    private final Type entityClass;
    private final Type requestClass;
    private final Type responseClass;
    public final Map<String, Method> endpointsGet = new HashMap<>();
    public final static ExampleMatcher MATCHER_ALL =
        ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase();
    public final static ExampleMatcher MATCHER_ANY =
        ExampleMatcher.matchingAny().withIgnoreNullValues().withIgnoreCase();

    // A constructor that will initialize the fields of the class.
    public MasterController(MasterService<ID, ENTITY> service) {
        this.service = service;
        final Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        this.entityClass = types[1];
        this.requestClass = types[2];
        this.responseClass = types[3];
    }

    /**
     * Prints the class name, the request and response classes, and the endpoints
     */
    public void printInfo() {
        System.out.println(getClass().getSimpleName());
        System.out.printf("Entity: %s \nRequest DTO: %s \nResponse DTO: %s \n",
                          this.entityClass, this.requestClass, this.responseClass);
        System.out.println("Endpoints(GET):");
        endpointsGet.values().forEach(System.out::println);
    }

    /**
     * The proxy is a static method that returns a reference to the current proxy
     *
     * @return The MasterController object.
     */
    protected final CTRL proxy() {
        return (CTRL) AopContext.currentProxy();
    }

    /**
     * This function maps an entity to a response DTO
     *
     * @param entity The entity to be mapped to the response DTO.
     * @return The response object.
     */
    public RESPONSE parseToResponseDto(ENTITY entity) {
        return modelMapper.map(entity, (Type) responseClass);
    }

    /**
     * It maps the response DTO to an entity
     *
     * @param response The response object that is returned from the API call.
     * @return The entity that was mapped from the response.
     */
    public ENTITY parseToEntity(RESPONSE response) {
        return modelMapper.map(response, (Type) entityClass);
    }

    /**
     * It maps the request DTO to an entity
     *
     * @param request The request object that is being mapped to an entity.
     * @return The mapped entity.
     */
    public ENTITY parseToEntity(REQUEST request) {
        return modelMapper.map(request, (Type) entityClass);
    }

    /**
     * It maps the entity page to a response DTO page.
     *
     * @param entityPage The page of entities to be converted to a response page.
     * @return The page of DTOs.
     */
    public ResponseEntity<?> craftResponsePage(@NotNull Page<ENTITY> entityPage) {
        final Page<?> responsePage = entityPage.map(this::parseToResponseDto);
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    //GET: ONE or ALL
    // The main GET method to delegate what type of database consult will be called: getOne or getAll.
    @ConsoleLog
    @GetMapping
    public ResponseEntity<?> get(
        @RequestParam(name = "id", required = false) Optional<ID> id,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens) {
        if (id.isPresent()) return proxy().getOne(id.get());
        return proxy().getAll(page, itens);
    }

    //GET: by ID
    // A method that returns a specific entity by a given ID.
    @ConsoleLog
    protected ResponseEntity<?> getOne(@NotNull ID id) {
        final ENTITY entity = service.findById(id);
        final RESPONSE dto = modelMapper.map(entity, (Type) responseClass);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //GET: ALL
    // A method that returns a page of all movies entities.
    @ConsoleLog
    public ResponseEntity<?> getAll(int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final Page<?> entitiesPage = service.findAll(pageConfig);
        final Page<?> responsePage = entitiesPage.map(entity -> modelMapper.map(entity, responseClass));
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    //GET: CUSTOM VAR
    // A reflection method that will try call one of the mapped methods in the endpointsGet field.
    @ConsoleLog
    @GetMapping(path = "/{var}/{value}")
    public ResponseEntity<?> call(
        @PathVariable @NotBlank String var, @PathVariable @NotBlank String value,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens)
    throws InvocationTargetException, IllegalAccessException {
        var = var.toLowerCase(Locale.ROOT);
        value = value.toLowerCase(Locale.ROOT);
        final Method methodCall = Optional.ofNullable(endpointsGet.get(var))
                                          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                                                         "Incorrect URL path"));
        final Object[] params = new Object[]{value, page, itens};
        return (ResponseEntity<?>) methodCall.invoke(this, params);
    }

}
