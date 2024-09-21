package br.com.ppw.dma.domain.evidencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EvidenciaResultadoConverter implements AttributeConverter<TipoEvidenciaStatus, String> {

    @Override
    public String convertToDatabaseColumn(TipoEvidenciaStatus tipo) {
        if(tipo == null) return null;
        return tipo.status;
    }

    @Override
    public TipoEvidenciaStatus convertToEntityAttribute(String texto) {
        return TipoEvidenciaStatus.identificar(texto).orElse(null);
    }
}
