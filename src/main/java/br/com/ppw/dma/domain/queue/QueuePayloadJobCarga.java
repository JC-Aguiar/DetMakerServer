package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.pipeline.execution.PipelineJobCargaDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QueuePayloadJobCarga {

    String nome;
    String conteudo;
    String tipo;


    public QueuePayloadJobCarga(@NonNull ExecFile file) {
        nome = file.getArquivoNome();
        conteudo = file.getArquivo();
        tipo = "tmp";
    }

    public QueuePayloadJobCarga(@NonNull PipelineJobCargaDTO dto) {
        nome = dto.getNome();
        conteudo = dto.getConteudo();
        tipo = dto.getTipo();
    }

}
