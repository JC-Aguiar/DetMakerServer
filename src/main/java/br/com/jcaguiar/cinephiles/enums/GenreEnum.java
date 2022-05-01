package br.com.jcaguiar.cinephiles.enums;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

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



}
