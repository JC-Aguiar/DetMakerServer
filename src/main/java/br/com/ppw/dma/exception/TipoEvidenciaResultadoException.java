package br.com.ppw.dma.exception;

import br.com.ppw.dma.domain.evidencia.TipoEvidenciaStatus;
import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TipoEvidenciaResultadoException extends RuntimeException {

    public TipoEvidenciaResultadoException(@NonNull String resultado) {
        super(mensagemErro(resultado));
    }

    private static String mensagemErro(@NonNull String resultado) {
        val tiposPermitidos = Arrays.stream(TipoEvidenciaStatus.values())
            .map(TipoEvidenciaStatus::getStatus)
            .collect(Collectors.joining(", "));
        return String.format(
            "Tipo de resultado '%s' inválido. Os únicos valores permitidos são: %s",
            resultado, tiposPermitidos);
    }

}
