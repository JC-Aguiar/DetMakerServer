package br.com.ppw.dma.domain.relatorio;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

public enum TiposDeTeste {

    UNITARIO("Unitário"),
    INTEGRADO("Integrado");

    @Getter public final String nome;

    TiposDeTeste(String nome) {
        this.nome = nome;
    }

    public static Optional<TiposDeTeste> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(TiposDeTeste.values())
            .filter(tipo -> tipo.nome.equalsIgnoreCase(texto.trim()))
            .findFirst();
    }

}
