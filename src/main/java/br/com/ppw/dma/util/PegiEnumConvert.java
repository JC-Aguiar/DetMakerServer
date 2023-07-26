package br.com.ppw.dma.util;

import br.com.ppw.dma.enums.PegiEnum;
import br.com.ppw.dma.exception.ConverterToFieldException;
import br.com.ppw.dma.exception.ConverterToJsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Converter
public class PegiEnumConvert implements AttributeConverter<PegiEnum, String> {

    @Override
    public String convertToDatabaseColumn(@NotNull PegiEnum attribute)
    {
        System.out.printf("Converting PegiEnum to String\n");
        try {
            return new ObjectMapper().writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new ConverterToJsonException();
        }
    }

    @Override
    public PegiEnum convertToEntityAttribute(@NotBlank String dbData)
    {
        System.out.printf("Converting String to PegiEnum\n", dbData);
        try {
            return new ObjectMapper().readValue(dbData, PegiEnum.class);
        } catch (JsonProcessingException e) {
            throw new ConverterToFieldException();
        }
    }

}
