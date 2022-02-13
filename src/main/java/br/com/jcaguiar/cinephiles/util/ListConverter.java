package br.com.jcaguiar.cinephiles.util;

import br.com.jcaguiar.cinephiles.exception.JsonToListException;
import br.com.jcaguiar.cinephiles.exception.ListToJsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
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
            throw new ListToJsonException();
        }
    }

    @SneakyThrows
    @Override
    public List<?> convertToEntityAttribute(String dbData)
    {
        try {
            return new ObjectMapper().readValue(dbData, List.class);
        } catch (JsonProcessingException e) {
            throw new JsonToListException();
        }
    }
}
