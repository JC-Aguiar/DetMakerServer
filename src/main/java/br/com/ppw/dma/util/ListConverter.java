package br.com.ppw.dma.util;

import br.com.ppw.dma.exception.ConverterToFieldException;
import br.com.ppw.dma.exception.ConverterToJsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Converter
public class ListConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(@NotNull List<String> attribute)
    {
        System.out.printf("Converting List %s to String\n", attribute.getClass().getSimpleName());
        try {
            return new ObjectMapper().writeValueAsString(attribute) ;
        } catch (JsonProcessingException e) {
            throw new ConverterToJsonException();
        }
    }

    @Override
    public List<String> convertToEntityAttribute(@NotBlank String dbData)
    {
        System.out.printf("Converting String %s to List\n", dbData);
        System.out.println();
        try {
            return (List<String>) new ObjectMapper().readValue(dbData, List.class);
        } catch (JsonProcessingException e) {
            throw new ConverterToFieldException();
        }
    }
}
