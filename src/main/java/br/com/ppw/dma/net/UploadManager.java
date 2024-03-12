package br.com.ppw.dma.net;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Optional;

import static br.com.ppw.dma.net.UploadManagerStatus.*;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
//TODO: Javadoc
public class UploadManager {

    private final String reference;
    private final Optional<File> file;
    private UploadManagerStatus status = NOT_DONE;


    public UploadManager(String reference, @NonNull File file) {
        this.reference = reference;
        if(reference == null || reference.isEmpty()) {
            setStatus(INVALID);
            this.file = Optional.empty();
        }
        else {
            setStatus(SUCCESS);
            this.file = Optional.of(file);
        }
    }

    public String getMensagemStatus() {
        return String.format("Arquivo '%s': %s", reference, status.message);
    }

    public UploadManager setStatus(UploadManagerStatus status) {
        if(status == INVALID) return this;
        this.status = status;
        if(status.error) log.warn(getMensagemStatus());
        else log.info(getMensagemStatus());
        return this;
    }

}
