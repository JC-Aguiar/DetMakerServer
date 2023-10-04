package br.com.ppw.dma.enums;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.util.Arrays;

public enum GenreEnum {
    DRAMA("Drama"),
    MYSTERY("Mistério"),
    HUMOR("Comédia"),
    PARODY("Paródia"),
    REAL_FACTS("História"),
    WAR("Guerra"),
    CRIME("Crime"),
    DOCUMENTARY("Documentário"),
    ACTION("Ação"),
    ADVENTURE("Aventura"),
    ANIMATION("Animação"),
    FAMILY("Família"),
    THRILLER("Thriller"),
    FICTION("Ficção científica"),
    FANTASY("Fantasia"),
    BIBLICAL("Bíblico"),
    MUSICAL("Música"),
    HORROR("Terror"),
    CLASSIC("Clássico"),
    ROMANCE("Romance"),
    WESTERN("Faroeste"),
    TV_MOVIE("Cinema TV"),
    CULT("Cult");

    @Getter
    final String name;

    GenreEnum(@NotBlank String name) {
        this.name = name;
    }

    public static GenreEnum checkEnum(@NotBlank String genreString) {
        final GenreEnum genreEnum = Arrays.stream(GenreEnum.values())
            .filter(g -> g.getName().equalsIgnoreCase(genreString))
            .findFirst()
            .orElseThrow();
        return genreEnum;
    }


}
