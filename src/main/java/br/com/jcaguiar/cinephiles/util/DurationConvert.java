package br.com.jcaguiar.cinephiles.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Converter(autoApply = true)
public class DurationConvert implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
//        System.out.println("Convert Duration to Long");
        return attribute.toNanos();
    }

    @Override
    public Duration convertToEntityAttribute(Long duration) {
//        System.out.println("Convert Long to Duration");
        return Duration.of(duration, ChronoUnit.NANOS);
    }
}
