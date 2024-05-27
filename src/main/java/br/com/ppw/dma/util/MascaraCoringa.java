package br.com.ppw.dma.util;

import lombok.NonNull;

import java.util.function.Function;
import java.util.regex.Pattern;

import static br.com.ppw.dma.util.FormatString.abstrairVariavel;


public enum MascaraCoringa {

    VARIAVEL_JS((texto) -> abstrairVariavel(texto,
        "\\{.*?\\}")),
    VARIAVEL_XML((texto) -> abstrairVariavel(texto,
        "\\<.*?\\>")),
    UNICO((texto) -> substituirCoringas(texto,
        "?",
        "SUBSET", "NUMERO")),
    DUPLO((texto) -> substituirCoringas(texto,
        "??",
        "MINUTO", "SEGUNDO", "BILLING")),
    QUADRUPLO((texto) -> substituirCoringas(texto,
        "????",
        "DATA", "HORA")),
    EXTENSO((texto) -> substituirCoringasDinamicamente(texto,
        "?",
        "AAAA", "HHMISS", "AA", "MM", "DD", "HH", "SS")),
    ;

    public final Function<String, String> algoritmo;

    MascaraCoringa(Function<String, String> algoritmo, String...coringas) {
        this.algoritmo = algoritmo;
    }

    public static String substituirCoringas(
        @NonNull String texto,
        @NonNull String substituto,
        @NonNull String...coringas) {
        //----------------------------------
        for(var coringa : coringas) {
            texto = texto.replace(coringa, substituto);
        }
        return texto;
    }

    public static String substituirCoringasDinamicamente(
        @NonNull String texto,
        @NonNull String substituto,
        @NonNull String...coringas) {
        //----------------------------------
        for(var coringa : coringas) {
            texto = texto.replace(coringa, substituto.repeat(coringa.length()));
        }
        return texto;
    }

}
