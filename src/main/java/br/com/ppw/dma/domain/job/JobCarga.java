package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.execFile.ExecFile;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobCarga {

    String nome;
    String conteudo;
    String tipo;

    public JobCarga(@NonNull ExecFile file) {
        nome = file.getArquivoNome();
        conteudo = file.getArquivo();
        tipo = "tmp";
    }

}
