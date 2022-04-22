package br.com.jcaguiar.cinephiles.enums;

public enum ScriptEnum {
    ORIGINAL("Original"),
    ADAPTATION("Adaptation");

    String source;


    ScriptEnum(String source) {
        this.source = source;
    }
}
