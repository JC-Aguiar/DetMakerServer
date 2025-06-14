package br.com.ppw.dma.domain.massa;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.ambiente.AmbienteService;
import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.cliente.ClienteService;
import br.com.ppw.dma.domain.master.MasterController;
import br.com.ppw.dma.domain.master.MasterSummary;
import br.com.ppware.api.MassaPreparada;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("gerar-massa")
@Slf4j
public class MassaTabelaController extends MasterController<Long, MassaTabela, MassaTabelaController> {

    private MassaTabelaService service;

    private ClienteService clienteService;

    private AmbienteService ambienteService;


    @Autowired
    public MassaTabelaController(
        MassaTabelaService service,
        ClienteService clienteService,
        AmbienteService ambienteService)
    {
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
            .map(MassaTabelaDTO::new)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Endpoint para salvar mapeamento de massas aleatórias para tabelas.
     *
     * @param ambienteId {@link Long} contendo o ID do {@link Cliente} em que a table será associada.
     * @param dto       {@link MassaTabelaDTO} em que há o mapeamento das column para determinada table
     * @return {@link ResponseEntity} com status 200 em caso de sucesso
     */
    @PostMapping("ambiente/{ambienteId}/test")
    public ResponseEntity<List<MassaPreparada>> teste(
        @PathVariable("ambienteId") Long ambienteId,
        @RequestBody MassaTabelaDTO dto)
    {
        log.info("Validando mapeamentos de massa no ambiente ID {}.", ambienteId);
//        log.info("Nenhuma alteração será de fato persistida no banco!");
        var ambiente = ambienteService.findById(ambienteId);
        var massa = service.updateMetadataAndMockValues(ambiente.acessoBanco(), List.of(dto));
        if(massa.stream().anyMatch(MassaPreparada::isErros)) {
            log.warn("Massa possui erros!");
            throw new RuntimeException(
                "Existem campos não mapeados com sucesso: " + massa
                    .stream()
                    .map(MassaPreparada::getColunasErro)
                    .flatMap(erros -> erros.keySet().stream())
                    .collect(Collectors.joining(", "))
            );
        }
        log.info("Massa gerada com sucesso.");
        return ResponseEntity.ok(massa);
//        var name = dto.getNome();
//        dto.setNome(NumeroAleatorio.XDigitosEmString(10));
//        service.delete(service.save(ambiente, dto)); //TODO: manter?
//        dto.setNome(name);
    }

    //TODO: esse método também deveria atualizar caso já exista
    /**
     * Endpoint para salvar mapeamento de massas aleatórias para tabelas.
     * @param clienteId {@link Long} contendo o ID do {@link Cliente} em que a table será associada.
     * @param dto {@link MassaTabelaDTO} em que há o mapeamento das column para determinada table
     * @return {@link ResponseEntity} com status 200 em caso de sucesso
     */
    @Transactional
    @PostMapping("cliente/{clienteId}")
    public ResponseEntity<String> save(
        @PathVariable("clienteId") Long clienteId,
        @Valid @RequestBody MassaTabelaDTO dto)
    {
        log.info("Salvando novo mapeamento de massa '{}' para cliente ID {}.", dto.getNome(), clienteId);
        var cliente = clienteService.findById(clienteId);
        service.save(cliente, dto);
        return ResponseEntity.ok("Massa salva com sucesso");
    }

    /**
     * Endpoint para gerar massas aleatórias para diferentes tabelas.
     * @param ambienteId {@link Long} contendo o ID do {@link Ambiente} em que será feito insert.
     * @param dtos um ou mais {@link MassaTabelaDTO} em que há o mapeamento das column para determinada table
     * @return {@link ResponseEntity} contendo o resumo {@link MasterSummary}<{@link String}>
     *           informando quais tabelas obtiveram sucesso ou falha
     */
    @Transactional
    @PostMapping("ambiente/{ambienteId}/map")
//    public ResponseEntity<MasterSummary<String>> generate(
    public ResponseEntity<MasterSummary<MassaPreparada>> generate(
        @PathVariable("ambienteId") Long ambienteId,
        @Valid @RequestBody MassaTabelaDTO...dtos)
    {
        log.info("Iniciando geração de massa para {} table(s).", dtos.length);
        val ambiente = ambienteService.findById(ambienteId);
        val ambienteBanco = AmbienteAcessoDTO.banco(ambiente);
        var resultadoDosInserts = service.newInserts(ambienteBanco, dtos);
//        return ResponseEntity.ok(service.parseSummary(resultadoDosInserts));
        return MasterSummary.toResponseEntity(resultadoDosInserts);
    }

    /**
     * Endpoint para gerar massas aleatórias para diferentes tabelas.
     * @param ambienteId {@link Long} contendo o ID do {@link Ambiente} em que será feito insert.
     * @param massaId um ou mais IDs do type {@link Long} que representa registros de mapeamento
     *                de uma table se suas respectivas column
     * @return {@link ResponseEntity} contendo o resumo {@link MasterSummary}<{@link String}>
     *          informando quais tabelas obtiveram sucesso ou falha
     */
    @Transactional
    @PostMapping("ambiente/{ambienteId}/ids/{ids}")
//    public ResponseEntity<MasterSummary<String>> generate(
    public ResponseEntity<MasterSummary<MassaPreparada>> generate(
        @PathVariable("ambienteId") Long ambienteId,
        @PathVariable("ids") Long...massaId)
    {
        log.info("Iniciando geração de massa para {} table(s).", massaId.length);;
        var massa = Arrays.stream(massaId)
            .map(service::findById)
            .map(MassaTabela::toDto)
            .toArray(MassaTabelaDTO[]::new);
        val ambiente = ambienteService.findById(ambienteId);
        val ambienteBanco = AmbienteAcessoDTO.banco(ambiente);
        var resultadoDosInserts = service.newInserts(ambienteBanco, massa);
        return MasterSummary.toResponseEntity(resultadoDosInserts);
//        return ResponseEntity.ok(service.parseSummary(resultadoDosInserts));
    }

}
