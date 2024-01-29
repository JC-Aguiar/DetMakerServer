package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.util.SqlUtils;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("query")
@Slf4j
public class ConfigQueryController extends MasterController<Long, ConfigQuery, ConfigQueryController> {

    private ConfigQueryService service;

    private AmbienteService ambienteService;


    public ConfigQueryController(
        @Autowired ConfigQueryService service,
        @Autowired AmbienteService ambienteService) {
        //--------------------------------------------
        super(service);
        this.service = service;
        this.ambienteService = ambienteService;
    }

    @Override
    public ResponseEntity<?> parseOne(ConfigQuery entity) {
        val comandoSqls = new ComandoSql(entity);
        return ResponseEntity.ok(comandoSqls);
    }

    @Override
    public ResponseEntity<?> parseAll(Page<ConfigQuery> configQueries) {
        val comandoSqls = configQueries.map(ComandoSql::new);
        return ResponseEntity.ok(comandoSqls);
    }

    //TODO: javadoc
    @GetMapping(value = "job/{jobId}")
    public ResponseEntity<List<ComandoSql>> getAllByJob(@PathVariable() Long jobId) {
        if(jobId == null) throw new RuntimeException("Informe o ID do Job.");

        log.info("Obtendo todas as ConfigQuery's do Job ID {}.", jobId);
        val resultado = service.findAllByJobId(jobId)
            .stream()
            .map(ComandoSql::new)
            .toList();
        log.info("Total de ConfigQuery's encontradas: {}", resultado.size());

        if(resultado.isEmpty()) {
            throw new NoSuchElementException(
                "Nenhuma ConfigQuery disponível para o Job ID " + jobId);
        }
        resultado.forEach(cq -> log.info(" - {}", cq));
        return ResponseEntity.ok(resultado);
    }

    //TODO: javadoc
    @PostMapping(value = "validade/dynamic/ambiente/{ambienteId}")
    public ResponseEntity<String> validarQuery(
        @PathVariable() Long ambienteId,
        @Valid @NonNull @RequestBody ComandoSql comandoSql) {
        //----------------------------------------------------------
        val ambiente = ambienteService.findById(ambienteId);
        val acessoBanco = AmbienteAcessoDTO.banco(ambiente);
        String mensagem = null;
        try {
            val sql = comandoSql.getSqlCompleta();
            log.info("Validando SQL: '{}'", sql); //TODO: adicionar ID da ConfigQuery
            service.validadeQuery(sql, acessoBanco);
            mensagem = "Query aprovada.";
            return ResponseEntity.ok(mensagem);
        }
        catch(SQLException | PersistenceException e) {
            mensagem = "Query reprovada: " + SqlUtils.getExceptionMainCause(e);
            log.info(mensagem);
            return ResponseEntity.badRequest().body(mensagem);
        }
        //catch() TODO: cath da Exceção personalizada
    }


    //TODO: javadoc
    @PostMapping(value = "validade/manual/ambiente/{ambienteId}")
    public ResponseEntity<String> validarQuery(
        @PathVariable() Long ambienteId,
        @Valid @NotBlank @RequestBody String sql) {
        //----------------------------------------------------------
        val ambiente = ambienteService.findById(ambienteId);
        val banco = AmbienteAcessoDTO.banco(ambiente);
        sql = sql.replace("\"", "");
        String mensagem = null;
        try {
            log.info("Validando SQL: '{}'", sql); //TODO: adicionar ID da ConfigQuery
            service.validadeQuery(sql, banco);
            mensagem = "Query aprovada.";
            log.info(mensagem);
            return ResponseEntity.ok(mensagem);
        }
        catch(SQLException | PersistenceException e) {
            mensagem = "Query reprovada: " + SqlUtils.getExceptionMainCause(e);
            log.info(mensagem);
            return ResponseEntity.badRequest().body(mensagem);
        }
    }
}
