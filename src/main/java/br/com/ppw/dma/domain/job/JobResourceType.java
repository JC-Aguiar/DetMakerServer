package br.com.ppw.dma.domain.job;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum JobResourceType {

    CARGA,
    REMESSA,
    LOG;


    public static Optional<JobResourceType> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(JobResourceType.values())
            .filter(tipo -> tipo.name().equalsIgnoreCase(texto))
            .findFirst();
    }
}
