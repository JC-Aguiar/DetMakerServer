package br.com.ppw.dma.domain.execFile;

import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

public enum TipoExecFile {

    CARGA("carga"),
    REMESSA("remessa"),
    LOG("log");

    @Getter public final String tipo;

    TipoExecFile(String tipo) {
        this.tipo = tipo;
    }

    public static Optional<TipoExecFile> identificar(String texto) {
        if(texto == null) return Optional.empty();
        return Stream.of(TipoExecFile.values())
            .filter(tipo -> tipo.tipo.equalsIgnoreCase(texto.trim()))
            .findFirst();
    }
}
