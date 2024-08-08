package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.util.SqlUtils;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("query")
@Slf4j
public class ConfigQueryController extends MasterController<Long, ConfigQuery, ConfigQueryController> {

    private ConfigQueryService service;

    private AmbienteService ambienteService;

    private JobService jobService;


    public ConfigQueryController(
        @Autowired ConfigQueryService service,
        @Autowired AmbienteService ambienteService,
        @Autowired JobService jobService) {
        //--------------------------------------------
        super(service);
        this.service = service;
        this.ambienteService = ambienteService;
        this.jobService = jobService;
    }

    @Override
    public ResponseEntity<?> parseOne(ConfigQuery entity) {
//        val comandoSqls = new ComandoSql(entity);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<?> parseAll(Page<ConfigQuery> configQueries) {
//        val comandoSqls = configQueries.map(ComandoSql::new);
        return ResponseEntity.ok(configQueries);
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<List<QueryInfoDTO>> getAllByCliente(
        @PathVariable(name = "clienteId") Long clienteId) {

        final List<QueryInfoDTO> dtos = service.findAllByCliente(clienteId)
            .stream()
            .map(QueryInfoDTO::new)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    //TODO: frontend não consta usando esse endoint, se ainda for usar
    // alterar o mapeamento ConfigQuery -> ComandoSQL
//    @GetMapping(value = "job/{jobId}")
//    public ResponseEntity<List<ComandoSql>> getAllByJob(@PathVariable Long jobId) {
//        if(jobId == null) throw new RuntimeException("Informe o ID do Job.");
//
//        log.info("Obtendo todas as ConfigQuery's do Job ID {}.", jobId);
//        val resultado = service.findAllByJobId(jobId)
//            .stream()
//            .map(ComandoSql::new)
//            .toList();
//        log.info("Total de ConfigQuery's encontradas: {}", resultado.size());
//
//        if(resultado.isEmpty()) {
//            throw new NoSuchElementException(
//                "Nenhuma ConfigQuery disponível para o Job ID " + jobId);
//        }
//        resultado.forEach(cq -> log.info(" - {}", cq));
//        return ResponseEntity.ok(resultado);
//    }

    //TODO: javadoc
    @PostMapping(value = "validade/dynamic/ambiente/{ambienteId}")
    public ResponseEntity<String> validarQuery(
        @PathVariable Long ambienteId,
        @RequestBody ComandoSql comandoSql) {
        //----------------------------------------------------------
        val ambiente = ambienteService.findById(ambienteId);
        val acessoBanco = AmbienteAcessoDTO.banco(ambiente);
        try {
            service.validadeQuery(comandoSql, acessoBanco);
            return ResponseEntity.ok("Query aprovada.");
        }
        catch(SQLException | PersistenceException e) {
            var mensagem = "Query reprovada: " + SqlUtils.getExceptionMainCause(e);
            log.warn(mensagem);
            return ResponseEntity.badRequest().body(mensagem);
        }
        //catch() TODO: cath da Exceção personalizada (?)
    }


    //TODO: javadoc
    @PostMapping(value = "validade/manual/ambiente/{ambienteId}")
    public ResponseEntity<String> validarQuery(
        @PathVariable Long ambienteId,
        @Valid @NotBlank @RequestBody String sql) {
        //----------------------------------------------------------
        val ambiente = ambienteService.findById(ambienteId);
        val banco = AmbienteAcessoDTO.banco(ambiente);
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
            log.warn(mensagem);
            return ResponseEntity.badRequest().body(mensagem);
        }
    }


    //TODO: javadoc
    @GetMapping(value = "{queryId}/vars")
    public ResponseEntity<List<FiltroSql>> obterVariaveisDaQuery(@PathVariable Long queryId) {
        var variaveis = service.getVarsFromQueryId(queryId);
        return ResponseEntity.ok(variaveis);
    }

    /**
     * Cria ou atualiza uma entidade {@link ConfigQuery} e suas respectivas {@link ConfigQueryVar} para
     * detemrinado cliente.
     * @param ambienteId {@link Long} obtido no path da uri
     * @param query {@link QueryInfoDTO}
     * @return {@link ResponseEntity} da mesma {@link QueryInfoDTO} enviada, so que atualizada com o banco.
     * @throws DuplicatedRecordException em caso de duplicidade no banco.
     */
    @PostMapping(value = "ambiente/{ambienteId}")
    public ResponseEntity<QueryInfoDTO> criarAtualizarQuery(
        @PathVariable Long ambienteId,
        @Valid @RequestBody QueryInfoDTO query)
    throws DuplicatedRecordException {

        val ambiente = ambienteService.findById(ambienteId);
        var banco = AmbienteAcessoDTO.banco(ambiente);
        var job = jobService.findById(query.getJobId());
        service.completeAndValidateVariables(query, banco);
        var configQuery = service.criarAtualizar(job, query);
        return ResponseEntity.ok(new QueryInfoDTO(configQuery));
    }

    @DeleteMapping("{queryId}")
    public ResponseEntity<String> deletarQuery(@PathVariable Long queryId) {
        service.delete(service.findById(queryId));
        log.info("Removida Query ID {}.", queryId);
        return ResponseEntity.ok("Query removida com sucesso.");
    }

    @DeleteMapping("{queryId}/var/{varId}")
    public ResponseEntity<QueryInfoDTO> deletarQueryVar(
        @PathVariable Long queryId,
        @PathVariable Long varId) {

        service.deleteVar(varId);
        log.info("Removida Variável ID {} da Query ID {}.", varId, queryId);
        var dto = new QueryInfoDTO(service.findById(queryId));
        return ResponseEntity.ok(dto);
    }

}
