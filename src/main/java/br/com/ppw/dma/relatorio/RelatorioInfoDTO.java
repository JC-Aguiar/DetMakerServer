package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.master.MasterRequestDTO;
import br.com.ppw.dma.master.MasterResponseDTO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatorioInfoDTO implements MasterRequestDTO, MasterResponseDTO {

    String idProjeto;
    String nomeProjeto;
    String nomeAtividade;
    String consideracoes;
    String testeTipo;
    //String sistema;
    //String ambiente;

    public RelatorioInfoDTO(@NonNull Relatorio relatorio) {
        this.idProjeto = relatorio.getIdProjeto();
        this.nomeProjeto = relatorio.getNomeProjeto();
        this.nomeAtividade = relatorio.getNomeAtividade();
        this.consideracoes = relatorio.getConsideracoes();
        //this.sistema = relatorio.getSistema();
        //this.ambiente = relatorio.getAmbiente();
        if(relatorio.getTesteTipo() != null)
            this.testeTipo = relatorio.getTesteTipo().nome;
    }

}
