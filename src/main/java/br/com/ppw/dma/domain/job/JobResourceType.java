package br.com.ppw.dma.domain.job;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum JobResourceType {

    CARGA("carga"),
    REMESSA("remessa"),
    LOG("log");

    public final String tipo;


    JobResourceType(String tipo) {
        this.tipo = tipo;
    }

    public static Optional<JobResourceType> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(JobResourceType.values())
            .filter(tipo -> tipo.tipo.equalsIgnoreCase(texto))
            .findFirst();
    }
}
