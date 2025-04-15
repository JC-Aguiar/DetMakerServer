package br.com.ppw.dma.domain.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class MasterController<ID, ENTITY extends MasterEntity<ID>, THIS extends MasterController> {

    @Getter MasterService service;
    Type entityClass;

    public final static ExampleMatcher MATCHER_ALL = ExampleMatcher
        .matchingAll()
        .withIgnoreNullValues()
        .withIgnoreCase();
    public final static ExampleMatcher MATCHER_ANY = ExampleMatcher
        .matchingAny()
        .withIgnoreNullValues()
        .withIgnoreCase();


    public MasterController(MasterService service) {
        this.service = service;
        final Type[] types = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
        this.entityClass = types[1];
    }

    @GetMapping("id/{id}")
    public ResponseEntity<?> getOne(@PathVariable(name = "id") ID id)
    throws NoSuchMethodException {
        final ENTITY entitie = (ENTITY) service.findById(id);
        return parseOne(entitie);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "itens", defaultValue = "12") int itens)
    throws NoSuchMethodException {
        final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final Page<ENTITY> entities = service.findAll(pageConfig);
        return parseAll(entities);
    }

    public abstract ResponseEntity<?> parseOne(ENTITY entity);

    public abstract ResponseEntity<?> parseAll(Page<ENTITY> entities);

    @Transactional
    @DeleteMapping("id/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") ID...id)
    throws NoSuchMethodException {
        var entities = (List<ENTITY>) service.findById(Set.of(id));
        var idsBanco = entities.stream()
            .map(MasterEntity::getId)
            .collect(Collectors.toSet());
        service.delete(entities);
        return ResponseEntity.ok(
            "IDs deletados: " + idsBanco + ".\n Total: " + idsBanco.size() + "."
        );
    }

}