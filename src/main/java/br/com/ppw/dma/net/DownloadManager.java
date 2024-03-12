package br.com.ppw.dma.net;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.ppw.dma.net.DownloadManagerStatus.*;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
//TODO: Javadoc
public class DownloadManager {

    private final String reference;
    private final Optional<RemoteFile> preFile;
    private Optional<RemoteFile> postFile = Optional.empty();
    private DownloadManagerStatus status = NOT_DONE;


    public DownloadManager(String reference, @NonNull Optional<RemoteFile> file) {
        this.reference = reference;
        if(reference == null || reference.isEmpty()) {
            setStatus(INVALID);
            preFile = Optional.empty();
        }
        else {
            preFile = file;
        }
    }

    public void setPostFile(Optional<RemoteFile> file) {
        if(status == INVALID) return;
        postFile = file;
        printFiles();

        if(postFile.isEmpty()) {
            setStatus(NOT_FOUND);
            return;
        }
        if(preFile.isPresent()) {
            if(preFile.get().iguais(postFile.get())) {
                setStatus(DUPLICATED);
            }
            else {
                setStatus(SUCCESS);
                val similaridade = postFile.get().statusSimilaridade(preFile.get());
                if(!similaridade.isEmpty()) log.info(similaridade);
            }
        }
        else {
            setStatus(SUCCESS);
        }
    }

    public boolean validarReferencia(@NotBlank String reference) {
        if(this.reference.equals(reference)) return true;
        log.trace("Incompatibilidade no gerenciamento de arquivos: a referência não é a mesma.");
        log.trace("Path-Reference atual: {}", this.reference);
        log.trace("Path-Reference nova: {}", reference);
        return false;
    }

    public String getMensagemStatus() {
        return String.format("Arquivo '%s': %s", reference, status.message);
    }

    public void setStatus(DownloadManagerStatus status) {
        if(status == INVALID) return;
        this.status = status;
        if(status.error) log.warn(getMensagemStatus());
        else log.info(getMensagemStatus());
    }

    public void printFiles() {
        if(status == NOT_DONE) {
            log.warn(status.message);
            return;
        }
        log.info("\t - Referência: '{}'", reference);
        log.info("\t - Arquivo pré-execução: {}", preFile);
        log.info("\t - Arquivo pós-execução: {}", postFile);
    }
}
