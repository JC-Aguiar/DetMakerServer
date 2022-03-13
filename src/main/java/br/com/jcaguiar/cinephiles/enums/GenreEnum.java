package br.com.jcaguiar.cinephiles.enums;

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

    final String name;

    GenreEnum(String name) {
        this.name = name;
    }
}
