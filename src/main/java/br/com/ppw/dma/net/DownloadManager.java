package br.com.ppw.dma.net;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
//TODO: Javadoc
public class DownloadManager {
    final String reference;
    @Setter Optional<RemoteFile> preFile = Optional.empty();
    Optional<RemoteFile> postFile = Optional.empty();
    final List<String> avisos = new ArrayList<>();

    private static final String ARQUIVO_PRE_AUSENTE = "Nenhum arquivo coletado antes da execução.";
    private static final String ARQUIVO_POS_AUSENTE = "Nenhum arquivo pós-execução coletado.";
    private static final String ARQUIVOS_SEMELHANTES = "Os arquivos pré e pós execução não são o mesmo, " +
        "apesar da semelhança: ";
    private static final String ARQUIVOS_DUPLICADOS = "Nenhum arquivo pós-execução identificado.";


    public DownloadManager(String reference) {
        this.reference = reference;
        if(reference == null || reference.isEmpty()) {
            addAviso("Nenhuma referência de diretório/arquivo especificada para download.");
        }
        //this.preFile = preFile;
        //if(this.preFile.isPresent()) avisos.add(ARQUIVO_POS_AUSENTE);
        //else avisos.add(ARQUIVO_PRE_AUSENTE);
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
                //avisos = ARQUIVOS_DUPLICADOS;
                log.warn(ARQUIVOS_DUPLICADOS);
            }
            else {
                val similaridade = postFile.get().statusSimilaridade(preFile.get());
                //avisos = similaridade.isEmpty() ? similaridade : ARQUIVOS_SEMELHANTES + similaridade;
                log.info(similaridade.isEmpty() ? similaridade : ARQUIVOS_SEMELHANTES + similaridade);
            }
        }
    }

    public void addAviso(@NonNull String mensagem) {
        avisos.add(mensagem);
        log.warn(mensagem);
    }

    public void printFiles() {
        log.info("\t - Referência: '{}'", reference);
        log.info("\t - Arquivo pré-execução: {}", preFile);
        log.info("\t - Arquivo pós-execução: {}", postFile);
    }
}
