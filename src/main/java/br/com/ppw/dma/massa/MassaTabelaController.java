package br.com.ppw.dma.massa;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.cliente.ClienteService;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.master.MasterSummaryDTO;
import br.com.ppware.NumeroAleatorio;
import br.com.ppware.api.GeradorDeMassa;
import br.com.ppware.api.MassaPreparada;
import br.com.ppware.api.MassaTabelaDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("gerar-massa")
@Slf4j
public class MassaTabelaController extends MasterController<Long, MassaTabela, MassaTabelaController> {

    private MassaTabelaService service;

    private ClienteService clienteService;

    private AmbienteService ambienteService;


    public MassaTabelaController(
        @Autowired MassaTabelaService service,
        @Autowired ClienteService clienteService,
        @Autowired AmbienteService ambienteService) {
        //--------------------------------------------
        super(service);
        this.service = service;
        this.clienteService = clienteService;
        this.ambienteService = ambienteService;
    }

    @Override
    public ResponseEntity<MassaTabelaDTO> parseOne(MassaTabela entity) {
        val dto = entity.toDto();
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<Page<MassaTabelaDTO>> parseAll(Page<MassaTabela> configQueries) {
        val dtos = configQueries.map(MassaTabela::toDto);
        return ResponseEntity.ok(dtos);
    }

    //TODO: javadoc
    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<List<MassaTabelaDTO>> getAllByCliente(@PathVariable("clienteId") Long clienteId) {
        List<MassaTabelaDTO> dtos = service.findAllByCliente(clienteId)
            .stream()
            .map(MassaTabela::toDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint para salvar mapeamento de massas aleatórias para tabelas.
     *
     * @param clienteId {@link Long} contendo o ID do {@link Cliente} em que a tabela será associada.
     * @param dto       {@link MassaTabelaDTO} em que há o mapeamento das colunas para determinada tabela
     * @return {@link ResponseEntity} com status 200 em caso de sucesso
     */
    @PostMapping("cliente/{clienteId}/test")
    public ResponseEntity<List<MassaPreparada>> teste(
        @PathVariable("clienteId") Long clienteId,
        @Valid @RequestBody MassaTabelaDTO dto) {
        //-----------------------------------------------
        log.info("Validando novo mapeamentos de massa para cliente ID {}.", clienteId);
        log.info("Nenhuma alteração será de fato persistida no banco!");
        var cliente = clienteService.findById(clienteId);
        var nome = dto.getNome();
        dto.setNome(NumeroAleatorio.XDigitosEmString(10));
        service.delete(service.save(cliente, dto));
        dto.setNome(nome);
        var massa = GeradorDeMassa.mapearMassa(dto);
        log.info("Massa gerada com sucesso.");
        log.info(massa.toString());
        return ResponseEntity.ok(massa);
    }

    /**
     * Endpoint para salvar mapeamento de massas aleatórias para tabelas.
     * @param clienteId {@link Long} contendo o ID do {@link Cliente} em que a tabela será associada.
     * @param dto {@link MassaTabelaDTO} em que há o mapeamento das colunas para determinada tabela
     * @return {@link ResponseEntity} com status 200 em caso de sucesso
     */
    @Transactional
    @PostMapping("cliente/{clienteId}")
    public ResponseEntity<String> save(
        @PathVariable("clienteId") Long clienteId,
        @Valid @RequestBody MassaTabelaDTO dto) {
        //-----------------------------------------------
        log.info("Salvando novos mapeamentos de massa para cliente ID {}.", clienteId);
        var cliente = clienteService.findById(clienteId);
        service.save(cliente, dto);
        return ResponseEntity.ok("Massa salva com sucesso");
    }

    /**
     * Endpoint para gerar massas aleatórias para diferentes tabelas.
     * @param ambienteId {@link Long} contendo o ID do {@link Ambiente} em que será feito insert.
     * @param dtos um ou mais {@link MassaTabelaDTO} em que há o mapeamento das colunas para determinada tabela
     * @return {@link ResponseEntity} contendo o resumo {@link MasterSummaryDTO}<{@link String}>
     *           informando quais tabelas obtiveram sucesso ou falha
     */
    @Transactional
    @PostMapping("ambiente/{ambienteId}/map")
//    public ResponseEntity<MasterSummaryDTO<String>> generate(
    public ResponseEntity<MasterSummaryDTO<MassaPreparada>> generate(
        @PathVariable("ambienteId") Long ambienteId,
        @Valid @RequestBody MassaTabelaDTO...dtos) {
        //-----------------------------------------------
        log.info("Iniciando geração de massa para {} tabela(s).", dtos.length);
        val ambiente = ambienteService.findById(ambienteId);
        val ambienteBanco = AmbienteAcessoDTO.banco(ambiente);
        var resultadoDosInserts = service.newInserts(ambienteBanco, dtos);
//        return ResponseEntity.ok(service.parseSummary(resultadoDosInserts));
        return MasterSummaryDTO.toResponseEntity(resultadoDosInserts);
    }

    /**
     * Endpoint para gerar massas aleatórias para diferentes tabelas.
     * @param ambienteId {@link Long} contendo o ID do {@link Ambiente} em que será feito insert.
     * @param massaId um ou mais IDs do tipo {@link Long} que representa registros de mapeamento
     *                de uma tabela se suas respectivas colunas
     * @return {@link ResponseEntity} contendo o resumo {@link MasterSummaryDTO}<{@link String}>
     *          informando quais tabelas obtiveram sucesso ou falha
     */
    @Transactional
    @PostMapping("ambiente/{ambienteId}/ids/{ids}")
//    public ResponseEntity<MasterSummaryDTO<String>> generate(
    public ResponseEntity<MasterSummaryDTO<MassaPreparada>> generate(
        @PathVariable("ambienteId") Long ambienteId,
        @PathVariable("ids") Long...massaId) {
        //-----------------------------------------------
        log.info("Iniciando geração de massa para {} tabela(s).", massaId.length);;
        var massa = Arrays.stream(massaId)
            .map(service::findById)
            .map(MassaTabela::toDto)
            .toArray(MassaTabelaDTO[]::new);
        val ambiente = ambienteService.findById(ambienteId);
        val ambienteBanco = AmbienteAcessoDTO.banco(ambiente);
        var resultadoDosInserts = service.newInserts(ambienteBanco, massa);
        return MasterSummaryDTO.toResponseEntity(resultadoDosInserts);
//        return ResponseEntity.ok(service.parseSummary(resultadoDosInserts));
    }

}
