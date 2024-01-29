package br.com.ppw.dma.cliente;

import br.com.ppw.dma.ambiente.AmbienteInfoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
