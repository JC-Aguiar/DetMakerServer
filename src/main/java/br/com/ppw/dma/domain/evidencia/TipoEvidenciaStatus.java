package br.com.ppw.dma.domain.evidencia;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

public enum TipoEvidenciaStatus {

    APROVADO("Aprovado"),
    PARCIAL("Parcial"),
    REPROVADO("Reprovado");

    @Getter public final String status;

    TipoEvidenciaStatus(String status) {
        this.status = status;
    }

    public static Optional<TipoEvidenciaStatus> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(TipoEvidenciaStatus.values())
            .filter(tipo -> tipo.status.equalsIgnoreCase(texto.trim()))
            .findFirst();
    }

}
