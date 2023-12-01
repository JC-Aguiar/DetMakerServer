package br.com.ppw.dma.relatorio;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TiposDeTesteConverter implements AttributeConverter<TiposDeTeste, String> {

    @Override
    public String convertToDatabaseColumn(TiposDeTeste tipo) {
        if(tipo == null) return null;
        return tipo.nome;
    }

    @Override
    public TiposDeTeste convertToEntityAttribute(String texto) {
        return TiposDeTeste.identificar(texto).orElse(null);
    }
}
