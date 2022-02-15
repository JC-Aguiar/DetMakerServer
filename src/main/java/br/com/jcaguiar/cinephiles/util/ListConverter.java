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
public class ListConverter implements AttributeConverter<List<String>, String> {

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(List<String> attribute)
    {
        System.out.printf("Converting List %s to String\n", attribute.getClass().getSimpleName());
        try {
            return new ObjectMapper().writeValueAsString(attribute) ;
        } catch (JsonProcessingException e) {
            throw new ListToJsonException();
        }
    }

    @SneakyThrows
    @Override
    public List<String> convertToEntityAttribute(String dbData)
    {
        System.out.printf("Converting String %s to List\n", dbData);
        System.out.println();
        try {
            return (List<String>) new ObjectMapper().readValue(dbData, List.class);
        } catch (JsonProcessingException e) {
            throw new JsonToListException();
        }
    }
}
