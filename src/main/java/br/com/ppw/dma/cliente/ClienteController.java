package br.com.ppw.dma.cliente;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.ambiente.AmbienteInfoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("cliente")
public class ClienteController {

    private final ClienteService clienteService;

    private final AmbienteService ambienteService;

    public ClienteController(
        @Autowired ClienteService clienteService,
        @Autowired AmbienteService ambienteService) {
        //--------------------------------------------
        this.clienteService = clienteService;
        this.ambienteService = ambienteService;
    }

    @GetMapping(value = "ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping
    public ResponseEntity<List<ClienteInfoDTO>> get() {
        //--------------------------------------------------------------
        //final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final List<ClienteInfoDTO> clientesDto = clienteService
            .findAll()
            .stream()
            .map(clt -> {
                final List<AmbienteInfoDTO> ambientesDto = ambienteService
                    .findAllFromCliente(clt)
                    .stream()
                    .map(AmbienteInfoDTO::new)
                    .toList();
                return new ClienteInfoDTO(clt, ambientesDto);
            })
            .toList();
        return ResponseEntity.ok(clientesDto);
    }

    @PostMapping("{clientId}/new/ambiente")
    public ResponseEntity<AmbienteInfoDTO> novoAmbiente(
        @PathVariable(name = "clientId") Long clientId,
        @Valid @RequestBody AmbienteInfoDTO dto) {
        //-----------------------------------------
        dto.setId(null);
        var cliente = clienteService.findById(clientId);
        var novoAmbiente = ambienteService.persist(new Ambiente(dto, cliente));
        dto.setId(novoAmbiente.getId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping()
    public ResponseEntity<ClienteInfoDTO> novoCliente(@Valid @RequestBody ClienteNovoDTO dto) {
        var novoCliente = clienteService.persist(new Cliente(dto));
        var dtoRetorno = new ClienteInfoDTO(novoCliente, List.of());
        return ResponseEntity.ok(dtoRetorno);
    }

}
