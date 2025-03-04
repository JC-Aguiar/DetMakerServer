package br.com.ppw.dma.domain.job;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum JobParameterType {

    STRING("\"%s\""),
    ANY("%s"),
    NUMBER("%d"),
    DATE("%s");

    public final String format;

    JobParameterType(String format) {
        this.format = format;
    }

    public static Optional<JobParameterType> identify(String name) {
        if(name == null || name.isBlank()) return Optional.empty();
        return Arrays.stream(JobParameterType.values())
            .filter(type -> type.name().equalsIgnoreCase(name))
            .findFirst();
    }
}
