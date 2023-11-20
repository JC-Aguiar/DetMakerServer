package br.com.ppw.dma.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Converter(autoApply = true)
public class DurationConvert implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        return attribute.toNanos();
    }

    @Override
    public Duration convertToEntityAttribute(Long duration) {
        return Duration.of(duration, ChronoUnit.NANOS);
    }
}
