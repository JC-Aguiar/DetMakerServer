package br.com.ppw.dma.domain.cliente;

import br.com.ppw.dma.domain.ambiente.*;
import br.com.ppw.dma.net.ConectorSftp;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("cliente")
public class ClienteController {

    private final ClienteService clienteService;

    private final AmbienteService ambienteService;


    public ClienteController(
        @Autowired ClienteService clienteService,
        @Autowired AmbienteService ambienteService)
    {
        this.clienteService = clienteService;
        this.ambienteService = ambienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteInfoDTO>> get() {
        //final Pageable pageConfig = PageRequest.of(page, itens, Sort.by("id").ascending());
        final List<ClienteInfoDTO> clientesDto = clienteService
            .findAll()
            .stream()
            .map(cliente -> {
                final List<AmbienteInfoDTO> ambientesDto = ambienteService
                    .findAllFromCliente(cliente)
                    .stream()
                    .map(AmbienteInfoDTO::new)
                    .toList();
                return new ClienteInfoDTO(cliente, ambientesDto);
            })
            .toList();
        return ResponseEntity.ok(clientesDto);
    }

    @PostMapping("{clientId}/new/ambiente")
    public ResponseEntity<AmbienteInfoDTO> novoAmbiente(
        @PathVariable(name = "clientId") Long clientId,
        @Valid @RequestBody NewAmbienteDTO dto)
    {
        var cliente = clienteService.findById(clientId);
        var novoAmbiente = ambienteService.persist(new Ambiente(dto, cliente));
        var result = new AmbienteInfoDTO(novoAmbiente);
        return ResponseEntity.ok(result);
    }

    //TODO: REMOVER
    @PostMapping(value = "{clientId}/ambiente/{ambienteNome}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> testarComando(
        @PathVariable() Long clientId,
        @PathVariable() String ambienteNome,
        @NonNull @RequestBody String comando)
    {
        var ambiente = clienteService.findById(clientId)
            .getAmbientes()
            .stream()
            .filter(amb -> amb.getNome().equalsIgnoreCase(ambienteNome))
            .findFirst()
            .orElseThrow();

        var sftp = ConectorSftp.conectar(AmbienteAcessoDTO.ftp(ambiente));

        //TODO: paliativo. Remover e aprimorar o código
        switch(ambiente.getConexaoSftp()) {
            case "10.129.226.157:22" -> ConectorSftp.setVivo1Properties(sftp);
            case "10.42.252.76:22" -> ConectorSftp.setVivo2Properties(sftp);
            case "10.129.164.206:22" -> ConectorSftp.setVivo3Properties(sftp);
        }
        var terminal = sftp.comando(comando);

        var mensagem = String.format("Exit-Code: %d.\n%s",
            terminal.getExitCode(), String.join("\n", terminal.getConsoleLog()));

        return ResponseEntity.ok(mensagem);
    }

    private static String lerOutput(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        var reader = new BufferedReader(new InputStreamReader(inputStream));
        String linha;
        while ((linha = reader.readLine()) != null) {
            sb.append(linha).append(System.lineSeparator());
        }
        return sb.toString();
    }

    @PostMapping()
    public ResponseEntity<ClienteInfoDTO> novoCliente(@Valid @RequestBody ClienteNovoDTO dto) {
        var novoCliente = clienteService.persist(new Cliente(dto));
        var dtoRetorno = new ClienteInfoDTO(novoCliente, List.of());
        return ResponseEntity.ok(dtoRetorno);
    }

}
