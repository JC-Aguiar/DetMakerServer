package br.com.ppw.dma.domain.queue.result;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import lombok.NonNull;

import java.util.Optional;

public record EvidenciaResult(Optional<Evidencia> evidencia, boolean exception, String detalhes) {

    public static EvidenciaResult ok(@NonNull Evidencia evidencia) {
        return new EvidenciaResult(Optional.of(evidencia), false, "");
    }

    public static EvidenciaResult erro(String detalhes) {
        return new EvidenciaResult(Optional.empty(), true, detalhes);
    }
}
