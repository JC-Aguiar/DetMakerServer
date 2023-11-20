package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.master.MasterController;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("queries")
@Slf4j
public class ConfigQueryController extends MasterController
    <Long, ConfigQuery, ConfigQueryInfoDTO, ComandoSql, ConfigQueryController> {

    private final ConfigQueryService service;

    public ConfigQueryController(@Autowired ConfigQueryService service) {
        super(service);
        this.service = service;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping(value = "job/{id}")
    public ResponseEntity<List<ComandoSql>> getAllByJob(@PathVariable() Long id) {
    log.info("Obtendo todas as ConfigQuery's para o Job ID {}", id);
    if(id == null) ResponseEntity.badRequest().body("Informe o ID de Job válido.");

    val resultado = service.findAllByJobId(id)
        .stream()
        .map(ComandoSql::new)
        .toList();
    log.info("Total de ConfigQuery's encontradas: {}", resultado.size());

    if(resultado.isEmpty())
        throw new NoSuchElementException("Nenhuma ConfigQuery disponível para o Job ID " + id);

    resultado.forEach(cq -> log.info(" - {}", cq));
    return ResponseEntity.ok(resultado);
    }

}
