package br.com.ppw.dma.enums;

public enum AgeEnum {
    PREHISTORY("Prehistory"),
    OLD_AGE("Old Age"),
    MEDIEVAL("Medieval"),
    RENAISSANCE("Renaissance"),
    MODERN("Modern"),
    SEX_XIX("Sec XIX"),
    SEX_XX("Sec XX"),
    YEAR_40("Years 40s"),
    YEAR_50("Years 50s"),
    YEAR_60("Years 60s"),
    YEAR_70("Years 70s"),
    YEAR_80("Years 80s"),
    YEAR_90("Years 90s"),
    YEAR_2000("Years 2000s"),
    YEAR_2010("Years 2010s"),
    YEAR_2020("Years 2020s"),
    FUTURISTIC("Futuristic");

    String age;


    AgeEnum(String age) {
        this.age = age;
    }
}
