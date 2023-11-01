package br.com.ppw.dma.master;

import br.com.ppw.dma.util.ConsoleLogAspect;
import br.com.ppw.dma.util.FormatString;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public abstract class MasterController<
    ID, ENTITY extends MasterEntity, REQUEST extends MasterRequestDTO,
    RESPONSE extends MasterResponseDTO, THIS extends MasterController> {

    @Getter @Autowired
    private ModelMapper modelMapper;

    @Getter
    private final MasterService service;

    private final Type entityClass;
    private final Type requestClass;
    private final Type responseClass;
    public final Map<String, Method> endpointsGet = new HashMap<>();
    public final static ExampleMatcher MATCHER_ALL =
        ExampleMatcher.matchingAll().withIgnoreNullValues().withIgnoreCase();
    public final static ExampleMatcher MATCHER_ANY =
        ExampleMatcher.matchingAny().withIgnoreNullValues().withIgnoreCase();

    // A constructor that will initialize the fields of the class.
    public MasterController(MasterService service) {
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
        final String initMessage = String.format(
            "Entity: %s - Request DTO: %s - Response DTO: %s",
            FormatString.subString(this.entityClass.getTypeName(), "\\." ),
            FormatString.subString(this.requestClass.getTypeName(), "\\."),
            FormatString.subString(this.responseClass.getTypeName(), "\\."));
        final String endpointsMessage = endpointsGet
            .values()
            .stream()
            .map(txt -> FormatString.subString(txt.getName(), "//."))
            .collect(Collectors.joining(", "));
        ConsoleLogAspect.LOGGER.info(initMessage);
        ConsoleLogAspect.LOGGER.info("Endpoints(GET): " + endpointsMessage);
    }

    /**
     * The proxy is a static method that returns a reference to the current proxy
     *
     * @return The MasterController object.
     */
    public final THIS proxy() {
        return (THIS) AopContext.currentProxy();
    }

    /**
     * This function maps an entity to a response DTO
     *
     * @param entity The entity to be mapped to the response DTO.
     * @return The response object.
     */
    public RESPONSE parseToDto(ENTITY entity) {
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

    //TODO: TESTE
    public ResponseEntity<?> craftResponse(
        @NotNull List<ENTITY> entityList,
        @NotNull Pageable pageConfig) {
        //-------------------------------------------------
        final PageImpl<ENTITY> responsePage = new PageImpl(
            entityList,
            pageConfig,
            entityList.size());
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

//    public ResponseEntity<?> craftResponsePage(
//        @NotNull MasterServiceLog<ENTITY> processEntity,
//        @NotNull Pageable pageConfig) {
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
//
//    public ResponseEntity<ENTITY> craftResponsePage(
//        @NotNull List<MasterServiceLog<ENTITY>> processEntity,
//        @NotNull Pageable pageConfig) {
//        final MasterControllerLog<ENTITY> result = new MasterControllerLog(processEntity);
//        return new ResponseEntity(result, HttpStatus.OK);
//    }

    public ResponseEntity<?> craftResponsePage(@NotNull Page<ENTITY> entityPage) {
        final Page<?> responsePage = entityPage.map(this::parseToDto);
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    //GET-ONE / GET-ALL
    // The main GET method to delegate what type of database consult will be called: getOne or getAll.
    @GetMapping
    public ResponseEntity<?> get(
        @RequestParam(name = "id", required = false) Optional<ID> id,
        @RequestParam(name = "page", defaultValue = "0") int page,      //TODO: valores padrões devem estar em variáveis estáticas globais.
        @RequestParam(name = "itens", defaultValue = "12") int itens)
    throws NoSuchMethodException {
        if (id.isPresent()) return proxy().getOne(id.get());
        return proxy().getAll(page, itens);
    }

    //GET-ONE (by ID)
    // A method that returns a specific entity by a given ID.
    //TODO: não deveria retornar uma Paginação usando também os atributos itens e page?
    public ResponseEntity<?> getOne(@NotNull ID id) throws NoSuchMethodException {
        final ENTITY entity = (ENTITY) service.findById(id);
        final RESPONSE dto = modelMapper.map(entity, responseClass);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    //GET-ALL
    // A method that returns a page of all movies entities.
    public ResponseEntity<?> getAll(int page, int itens) {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final Page<?> entitiesPage = service.findAll(pageConfig);
        final Page<?> responsePage = entitiesPage.map(entity -> modelMapper.map(entity, responseClass));
        return new ResponseEntity<>(responsePage, HttpStatus.OK);
    }

    //GET CUSTOM-PATH
    // A reflection method that will try call one of the mapped methods in the endpointsGet field.
//    @GetMapping(path = "/{var}/{value}")
//    public ResponseEntity<?> call(
//        @PathVariable @NotBlank String var,
//        @PathVariable @NotBlank String value,
//        @RequestParam(name = "page", defaultValue = "0") int page,
//        @RequestParam(name = "itens", defaultValue = "12") int itens)
//    throws InvocationTargetException, IllegalAccessException {
//        var = var.toLowerCase(Locale.ROOT);
//        value = value.toLowerCase(Locale.ROOT);
//        final Method methodCall = Optional
//            .ofNullable(endpointsGet.get(var))
//            .orElseThrow(() -> new ResponseStatusException(
//                HttpStatus.NOT_FOUND,
//                "Incorrect URL path")
//            );
//        final Object[] params = new Object[]{value, page, itens};
//        return (ResponseEntity<?>) methodCall.invoke(this, params);
//    }

}
