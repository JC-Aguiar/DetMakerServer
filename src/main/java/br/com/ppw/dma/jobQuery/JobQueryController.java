package br.com.ppw.dma.jobQuery;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.job.JobService;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.SqlSintaxe;
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
public class JobQueryController extends MasterController<Long, JobQuery, JobQueryController> {

    private JobQueryService queryService;

    private AmbienteService ambienteService;

    private JobService jobService;

    @Autowired
    public JobQueryController(
        JobQueryService queryService,
        AmbienteService ambienteService,
        JobService jobService) {
        //--------------------------------
        super(queryService);
        this.queryService = queryService;
        this.ambienteService = ambienteService;
        this.jobService = jobService;
    }

    @Override
    public ResponseEntity<?> parseOne(JobQuery entity) {
//        val comandoSqls = new ComandoSql(entity);
        return ResponseEntity.ok(entity);
    }

    @Override
    public ResponseEntity<?> parseAll(Page<JobQuery> configQueries) {
//        val comandoSqls = configQueries.map(ComandoSql::new);
        return ResponseEntity.ok(configQueries);
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<List<QueryInfoDTO>> getAllByCliente(
        @PathVariable(name = "clienteId") Long clienteId) {

        final List<QueryInfoDTO> dtos = queryService.findAllByCliente(clienteId)
            .stream()
            .map(QueryInfoDTO::new)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    //TODO: frontend não consta usando esse endoint, se ainda for usar
    // alterar o mapeamento JobQuery -> ComandoSQL
//    @GetMapping(value = "job/{jobId}")
//    public ResponseEntity<List<ComandoSql>> getAllByJob(@PathVariable Long jobId) {
//        if(jobId == null) throw new RuntimeException("Informe o ID do Job.");
//
//        log.info("Obtendo todas as JobQuery's do Job ID {}.", jobId);
//        val resultado = service.findAllByJobId(jobId)
//            .stream()
//            .map(ComandoSql::new)
//            .toList();
//        log.info("Total de JobQuery's encontradas: {}", resultado.size());
//
//        if(resultado.isEmpty()) {
//            throw new NoSuchElementException(
//                "Nenhuma JobQuery disponível para o Job ID " + jobId);
//        }
//        resultado.forEach(cq -> log.info(" - {}", cq));
//        return ResponseEntity.ok(resultado);
//    }

    //TODO: javadoc
    //      sincronizar com front !
    @PostMapping(value = "validade/ambiente/{ambienteId}")
    public ResponseEntity<String> validarQuery(
        @PathVariable Long ambienteId,
        @Valid @NotBlank @RequestBody String sql) {
        //----------------------------------------------------------
        val ambiente = ambienteService.findById(ambienteId);
        val banco = AmbienteAcessoDTO.banco(ambiente);
        String mensagem = null;
        try {
            log.info("Validando SQL: '{}'", sql);
            queryService.validadeQuery(sql, banco);
            mensagem = "Query aprovada.";
            log.info(mensagem);
            return ResponseEntity.ok(mensagem);
        }
        catch(SQLException | PersistenceException e) {
            mensagem = "Query reprovada: " + SqlSintaxe.getExceptionMainCause(e);
            log.warn(mensagem);
            return ResponseEntity.badRequest().body(mensagem);
        }
    }


    //TODO: sincronizar com front !!!
//    @GetMapping(value = "{queryId}/vars")
//    public ResponseEntity<List<FiltroSql>> obterVariaveisDaQuery(@PathVariable Long queryId) {
//        var variaveis = service.getVarsFromQueryId(queryId);
//        return ResponseEntity.ok(variaveis);
//    }

    //TODO: sincronizar com front !!!
    /**
     * Cria ou atualiza uma entidade {@link JobQuery} para determinado cliente.
     * @param ambienteId {@link Long} obtido no path da uri
     * @param dto {@link QueryInfoDTO}
     * @return {@link ResponseEntity} da mesma {@link QueryInfoDTO} enviada, so que atualizada com o banco.
     * @throws DuplicatedRecordException em caso de duplicidade no banco.
     */
    @PostMapping(value = "ambiente/{ambienteId}")
    public ResponseEntity<QueryInfoDTO> criarAtualizarQuery(
        @PathVariable Long ambienteId,
        @Valid @RequestBody QueryInfoDTO dto)
    throws DuplicatedRecordException, SQLException {

        val ambiente = ambienteService.findById(ambienteId);
        var banco = AmbienteAcessoDTO.banco(ambiente);
        var job = jobService.findById(dto.getJobId());
        queryService.validadeQuery(dto.getSql(), banco);
        var configQuery = queryService.criarAtualizar(job, dto);
        return ResponseEntity.ok(new QueryInfoDTO(configQuery));
    }

    @DeleteMapping("{queryId}")
    public ResponseEntity<String> deletarQuery(@PathVariable Long queryId) {
        queryService.delete(queryService.findById(queryId));
        log.info("Removida Query ID {}.", queryId);
        return ResponseEntity.ok("Query removida com sucesso.");
    }


}
