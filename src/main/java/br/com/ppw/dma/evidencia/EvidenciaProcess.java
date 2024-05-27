package br.com.ppw.dma.evidencia;

import lombok.NonNull;

import java.util.Optional;

public record EvidenciaProcess(Optional<Evidencia> evidencia, boolean exception, String detalhes) {

    public static EvidenciaProcess ok(@NonNull Evidencia evidencia) {
        return new EvidenciaProcess(Optional.of(evidencia), false, "");
    }

    public static EvidenciaProcess erro(String detalhes) {
        return new EvidenciaProcess(Optional.empty(), true, detalhes);
    }
}
