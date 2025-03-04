package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.pipeline.execution.PipelineJobCargaDTO;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueuePayloadJobCarga {

    String diretorio;

    String nome;

    @ToString.Exclude
    String conteudo;

    String tipo;


    public QueuePayloadJobCarga(@NonNull ExecFile file) {
        var lastSlashIndex = file.getMascara().lastIndexOf("/");
        diretorio = file.getMascara().substring(0, lastSlashIndex);
        nome = file.getArquivoNome();
        conteudo = file.getArquivo();
        tipo = "tmp";
    }

    public QueuePayloadJobCarga(@NonNull PipelineJobCargaDTO dto) {
        diretorio = dto.getDiretorio();
        nome = dto.getNome();
        conteudo = dto.getConteudo();
        tipo = dto.getTipo();
    }

    @ToString.Include(name = "conteudo")
    private String getResumoConteudo() {
        val tamanho = FormatString.contarSubstring(conteudo, "\n");
        var peso = conteudo.getBytes().length;
        return String.format("[linhas=%d, peso=%dKbs]", tamanho, peso);
    }

    @JsonIgnore
    public boolean validName() {
        return getNome() != null && !getNome().isBlank();
    }

}
