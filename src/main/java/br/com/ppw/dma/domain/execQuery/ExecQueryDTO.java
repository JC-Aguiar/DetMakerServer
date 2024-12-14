package br.com.ppw.dma.domain.execQuery;

import br.com.ppw.dma.domain.execFile.AnexoInfoDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecQueryDTO {

//    Long id;
    String query;
    String nome;
    String descricao;
    @ToString.Exclude String tabelaPreJob;
    @ToString.Exclude String tabelaPosJob;
    String inconformidade;


    public ExecQueryDTO(@NonNull ExecQuery execQuery) {
//        log.info("Convertendo Evidencia Nº{} em {}.", ordem, ExecQueryDTO.class.getSimpleName());
        this.query = execQuery.getQuery();
        this.nome = execQuery.getQueryNome();
        this.descricao = execQuery.getQueryDescricao();
        this.tabelaPreJob = execQuery.getResultadoPreJob();
        this.tabelaPosJob = execQuery.getResultadoPosJob();
        this.inconformidade = execQuery.getInconformidade();
//        log.info(this.toString());
    }

    @JsonIgnore
    @ToString.Include(name = "tabelasPreJob")
    public String getResumoTabelasPreJob() {
        return String.format("[registros=%d]", tabelaPreJob.lines().count());
    }

    @JsonIgnore
    @ToString.Include(name = "tabelasPosJob")
    public String getResumoTabelasPosJob() {
        return String.format("[registros=%d]", tabelaPosJob.lines().count());
    }

    private String getResumo(@NonNull List<AnexoInfoDTO> anexos) {
        val tamanho = anexos.size();
        val peso = anexos.stream()
            .mapToLong(AnexoInfoDTO::getPeso)
            .sum();
        return String.format("[quantidade=%d, peso=%dKbs]", tamanho, peso);
    }
}


