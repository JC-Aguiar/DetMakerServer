package br.com.ppw.dma.domain.job;

import java.util.Optional;
import java.util.stream.Stream;

public enum JobResourceAccessType {
    FTP,
    HTTP;


    public static Optional<JobResourceAccessType> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(JobResourceAccessType.values())
            .filter(tipo -> tipo.name().equalsIgnoreCase(texto))
            .findFirst();
    }

}
