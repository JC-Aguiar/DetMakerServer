package br.com.jcaguiar.cinephiles.master;

import br.com.jcaguiar.cinephiles.util.ConsoleLog;
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
public abstract class MasterController
    <ID, ENTITY extends MasterEntity, REQUEST extends MasterDtoRequest, RESPONSE extends MasterDtoResponse> {

    @Autowired
    private ModelMapper modelMapper;
    private final MasterService<ID, ENTITY> service;
    private final Type entityClass;
    private final Type requestClass;
    private final Type responseClass;
    public static final Map<String, Method> ENDPOINTS_GET = new HashMap<>();

    public final static ExampleMatcher MATCHER_ALL =
        ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase();

    public final static ExampleMatcher MATCHER_ANY =
        ExampleMatcher.matchingAny().withIgnoreNullValues().withIgnoreCase();

    public MasterController(Class<? extends MasterController<ID, ENTITY, REQUEST, RESPONSE>> subClass,
                            MasterService<ID, ENTITY> service)
    {
        this.service = service;
        final Type[] types = ((ParameterizedType) getClass().getGenericSuperclass())
            .getActualTypeArguments();
        this.entityClass = types[1];
        this.requestClass = types[2];
        this.responseClass = types[3];
    }

    public void printInfo()
    {
        System.out.println(getClass().getSimpleName());
        System.out.printf("Entity: %s \nRequest DTO: %s \nResponse DTO: %s \n",
            this.entityClass, this.requestClass, this.responseClass);
        System.out.println("Endpoints(GET):");
        ENDPOINTS_GET.values().forEach(System.out::println);
    }

    private static final MasterController PROXY()
    {
        return (MasterController) AopContext.currentProxy();
    }

    public RESPONSE parseToResponseDto(ENTITY entity)
    {
        return modelMapper.map(entity, (Type) responseClass);
    }

    public ENTITY parseToEntity(RESPONSE response)
    {
        return modelMapper.map(response, (Type) entityClass);
    }

    public ENTITY parseToEntity(REQUEST request)
    {
        return modelMapper.map(request, (Type) entityClass);
    }

    public ResponseEntity<?> craftResponsePage(@NotNull Page<ENTITY> entityPage)
    {
        final Page<?> responsePage = entityPage.map(this::parseToResponseDto);
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    //GET: FILTER
    @ConsoleLog
    @GetMapping
    public ResponseEntity<?> get(@RequestParam(name = "id", required = false) Optional<ID> id,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "itens", defaultValue = "12") int itens)
    {
        if(id.isPresent()) return PROXY().getOne(id.get());
        return PROXY().getAll(page, itens);
    }

    //GET: by ID
    @ConsoleLog
    protected ResponseEntity<?> getOne(@NotNull ID id)
    {
        final ENTITY entity = service.findById(id);
        final RESPONSE dto = modelMapper.map(entity, (Type) responseClass);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //GET: ALL
    @ConsoleLog
    public ResponseEntity<?> getAll(int page, int itens)
    {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final Page<?> entitiesPage = service.findAll(pageConfig);
        final Page<?> responsePage = entitiesPage.map(entity -> modelMapper.map(entity, responseClass));
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    //GET: CUSTOM VAR
    @ConsoleLog
    @GetMapping(path = "/{var}/{value}")
    public ResponseEntity<?> call(@PathVariable @NotBlank String var,
                                  @PathVariable @NotBlank String value,
                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                  @RequestParam(name = "itens", defaultValue = "12") int itens)
    throws InvocationTargetException, IllegalAccessException
    {
        var = var.toLowerCase(Locale.ROOT);
        value = value.toLowerCase(Locale.ROOT);
        final Method methodCall = Optional.ofNullable(ENDPOINTS_GET.get(var))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incorrect URL path"));
        final Object[] params = new Object[] { value, page, itens };
        return (ResponseEntity<?>) methodCall.invoke(this, params);
    }

}
