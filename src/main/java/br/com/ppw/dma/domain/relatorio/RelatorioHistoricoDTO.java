package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.evidencia.EvidenciaInfoDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioHistoricoDTO {

    Long id;
    String ticket;
    String pipeline;
    String descricao;
    String idProjeto;
    String nomeProjeto;
    String nomeAtividade;
    String consideracoes;
    String inconformidades;
    String testeTipo;
    String cliente;
    String ambiente;
    boolean sucesso;
    String erroFatal;
    String usuario;
    final List<EvidenciaInfoDTO> evidencias = new ArrayList<>();
    OffsetDateTime data;


    public RelatorioHistoricoDTO(@NonNull Relatorio relatorio) {
        log.info("Convertendo Relatorio em {}.", RelatorioHistoricoDTO.class.getSimpleName());
        this.id = relatorio.getId();
        this.ticket = relatorio.getTicket();
        this.pipeline = relatorio.getPipelineNome();
        this.descricao = relatorio.getPipelineDescricao();
        this.idProjeto = relatorio.getIdProjeto();
        this.nomeProjeto = relatorio.getNomeProjeto();
        this.nomeAtividade = relatorio.getNomeAtividade();
        this.inconformidades = relatorio.getInconformidades();
        this.cliente = relatorio.getCliente();
        this.ambiente = relatorio.getAmbiente().getNome();
        this.data = relatorio.getDataCompleta();
        this.sucesso = relatorio.getSucesso();
        this.erroFatal = relatorio.getErroFatal();
        this.usuario = relatorio.getUsuario();
        if(relatorio.getTesteTipo() != null)
            this.testeTipo = relatorio.getTesteTipo().nome;
        val cont = new AtomicInteger(0);
        relatorio.getEvidencias()
            .stream()
            .map(ev -> new EvidenciaInfoDTO(ev, cont.getAndIncrement()))
            .forEach(evidencias::add);
        log.info(this.toString());
    }

    @JsonIgnore
    public String getIdentificadorProjeto() {
        return idProjeto + "-" + nomeProjeto;
    }

}
