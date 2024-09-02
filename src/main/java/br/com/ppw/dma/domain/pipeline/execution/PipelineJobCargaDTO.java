package br.com.ppw.dma.domain.pipeline.execution;

import br.com.ppw.dma.domain.execFile.ExecFile;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineJobCargaDTO {

    String nome;
    String conteudo;
    String tipo;

    public PipelineJobCargaDTO(@NonNull ExecFile file) {
        nome = file.getArquivoNome();
        conteudo = file.getArquivo();
        tipo = "tmp";
    }

}
