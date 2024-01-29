package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.evidencia.EvidenciaInfoDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioHistoricoDTO {

    Long id;
    String pipeline;
    String idProjeto;
    String nomeProjeto;
    String nomeAtividade;
    String consideracoes;
    String parametros;
    String dados;
    String testeTipo;
    String cliente;
    String ambiente;
    final List<EvidenciaInfoDTO> evidencias = new ArrayList<>();
    OffsetDateTime data;
    Boolean sucesso;

    public RelatorioHistoricoDTO(@NonNull Relatorio relatorio) {
        log.info("Convertendo Relatorio em RelatorioHistoricoDTO.");
        this.id = relatorio.getId();
//        this.pipeline = relatorio.getPipeline().getProps().getNome();
        this.pipeline = relatorio.getPipeline().getNome();
        this.idProjeto = relatorio.getIdProjeto();
        this.nomeProjeto = relatorio.getNomeProjeto();
        this.nomeAtividade = relatorio.getNomeAtividade();
        this.consideracoes = relatorio.getConsideracoes();
        this.parametros = relatorio.getParametros();
        this.dados = relatorio.getPipeline().getDescricao();
        this.cliente = relatorio.getCliente();
        this.ambiente = relatorio.getAmbiente().getNome();
        this.data = relatorio.getData();
        this.sucesso = relatorio.getSucesso();
        if(relatorio.getTesteTipo() != null)
            this.testeTipo = relatorio.getTesteTipo().nome;

        val cont = new AtomicInteger(0);
        relatorio.getEvidencias()
            .stream()
            .map(ev -> new EvidenciaInfoDTO(ev, cont.getAndIncrement()))
            .forEach(evidencias::add);

        log.info(this.toString());
    }

}
