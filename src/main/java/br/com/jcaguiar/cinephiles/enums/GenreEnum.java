package br.com.jcaguiar.cinephiles.enums;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

public enum GenreEnum {
    DRAMA("Drama"),
    MYSTERY("Mistério"),
    HUMOR("Comédia"),
    PARODY("Paródia"),
    REAL_FACTS("Fatos Reais"),
    DOCUMENTARY("Documentário"),
    ACTION("Ação"),
    ADVENTURE("Aventura"),
    THRILLER("Thriller"),
    FICTION("Ficção científica"),
    FANTASY("Fantasia"),
    BIBLICAL("Bíblico"),
    MUSICAL("Musical"),
    HORROR("Terror"),
    CLASSIC("Clássico"),
    ROMANCE("Romance"),
    CULT("Cult");

    @Getter
    final String name;

    GenreEnum(@NotBlank String name) {
        this.name = name;
    }



}
