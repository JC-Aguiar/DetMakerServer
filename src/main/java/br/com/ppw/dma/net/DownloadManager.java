package br.com.ppw.dma.net;

import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
//TODO: Javadoc
public class DownloadManager {
    final String reference;
    final Optional<RemoteFile> preFile;
    Optional<RemoteFile> postFile = Optional.empty();
    String aviso = SEM_ARQUIVOS;

    private static final String SEM_ARQUIVOS = "Nenhum arquivo coletado pré ou pós execução.";
    private static final String ARQUIVO_POS_AUSENTE = "Nenhum arquivo pós-execução coletado.";
    private static final String ARQUIVOS_SEMELHANTES = "Os arquivos pré e pós execução não são o mesmo, " +
        "apesar da semelhança: ";
    private static final String ARQUIVOS_DUPLICADOS = "Nenhum arquivo pós-execução identificado.";


    public DownloadManager(@NotBlank String reference, Optional<RemoteFile> preFile) {
        this.reference = reference;
        this.preFile = preFile;
        if(this.preFile.isPresent()) aviso = ARQUIVO_POS_AUSENTE;
    }

    public void setPostFile(@NotBlank String reference, Optional<RemoteFile> file) {
        if(file.isEmpty()) return;
        if(!this.reference.equals(reference)) {
            log.trace("Incompatibilidade no gerenciamento de arquivos: a referência não é a mesma.");
            log.trace("Path-Reference atual: {}", this.reference);
            log.trace("Path-Reference nova: {}", reference);
            return;
        }
        postFile = file;
        printFiles();

        if(preFile.isPresent() && postFile.isPresent()) {
            if(preFile.get().iguais(postFile.get())) {
                aviso = ARQUIVOS_DUPLICADOS;
                log.warn(aviso);
            }
            else {
                val similaridade = postFile.get().statusSimilaridade(preFile.get());
                aviso = similaridade.isEmpty() ? similaridade : ARQUIVOS_SEMELHANTES + similaridade;
                log.info(aviso);
            }
        }
    }

    public void printFiles() {
        log.info("\t - Referência: '{}'", reference);
        log.info("\t - Arquivo pré-execução: {}", preFile);
        log.info("\t - Arquivo pós-execução: {}", postFile);
    }
}
