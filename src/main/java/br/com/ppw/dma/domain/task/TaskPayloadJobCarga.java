package br.com.ppw.dma.domain.task;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.pipeline.execution.PipelineJobCargaDTO;
import br.com.ppw.dma.util.FormatString;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskPayloadJobCarga {

    String nome;

    @ToString.Exclude
    String conteudo;

    String tipo;


    public TaskPayloadJobCarga(@NonNull ExecFile file) {
        nome = file.getArquivoNome();
        conteudo = file.getArquivo();
        tipo = "tmp";
    }

    public TaskPayloadJobCarga(@NonNull PipelineJobCargaDTO dto) {
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

}
