package br.com.ppw.dma.domain.execFile;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoExecFileConverter implements AttributeConverter<TipoExecFile, String> {

    @Override
    public String convertToDatabaseColumn(TipoExecFile tipo) {
        if(tipo == null) return null;
        return tipo.tipo;
    }

    @Override
    public TipoExecFile convertToEntityAttribute(String texto) {
        return TipoExecFile.identificar(texto).orElse(null);
    }
}
