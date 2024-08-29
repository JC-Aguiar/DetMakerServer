package br.com.ppw.dma.domain.evidencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EvidenciaResultadoConverter implements AttributeConverter<TipoEvidenciaResultado, String> {

    @Override
    public String convertToDatabaseColumn(TipoEvidenciaResultado tipo) {
        if(tipo == null) return null;
        return tipo.status;
    }

    @Override
    public TipoEvidenciaResultado convertToEntityAttribute(String texto) {
        return TipoEvidenciaResultado.identificar(texto).orElse(null);
    }
}
