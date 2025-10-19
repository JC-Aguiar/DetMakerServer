package br.com.ppw.dma.domain.job;

import java.util.Optional;
import java.util.stream.Stream;

public enum JobResourceProtocolType {
    FTP,
    HTTP;


    public static Optional<JobResourceProtocolType> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(JobResourceProtocolType.values())
            .filter(tipo -> tipo.name().equalsIgnoreCase(texto))
            .findFirst();
    }

}
