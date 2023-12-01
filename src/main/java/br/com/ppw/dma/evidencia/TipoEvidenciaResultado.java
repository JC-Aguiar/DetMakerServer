package br.com.ppw.dma.evidencia;

import lombok.Getter;
import lombok.NonNull;

import java.util.Optional;
import java.util.stream.Stream;

public enum TipoEvidenciaResultado {

    APROVADO("Aprovado"),
    PARCIAL("Parcial"),
    REPROVADO("Reprovado");

    @Getter public final String status;

    TipoEvidenciaResultado(String status) {
        this.status = status;
    }

    public static Optional<TipoEvidenciaResultado> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(TipoEvidenciaResultado.values())
            .filter(tipo -> tipo.status.equalsIgnoreCase(texto.trim()))
            .findFirst();
    }

}
