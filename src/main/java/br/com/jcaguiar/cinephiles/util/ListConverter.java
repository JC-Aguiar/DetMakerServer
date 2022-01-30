package br.com.jcaguiar.cinephiles.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.InvalidObjectException;
import java.util.List;

@Converter
public class ListConverter implements AttributeConverter<List<?>, String> {

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(List<?> attribute)
    {
        try {
            return new ObjectMapper().writeValueAsString(attribute) ;
        } catch (JsonProcessingException e) {
            throw new InvalidObjectException("Unexpected error while converting Entity to JSON");
        }
    }

    @SneakyThrows
    @Override
    public List<?> convertToEntityAttribute(String dbData)
    {
        try {
            return new ObjectMapper().readValue(dbData, List.class);
        } catch (JsonProcessingException e) {
            throw new InvalidObjectException("Unexpected error while converting JSON to Entity");
        }
    }
}
