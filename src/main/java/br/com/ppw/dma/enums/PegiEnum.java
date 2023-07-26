package br.com.ppw.dma.enums;

public enum PegiEnum {
    ALL("All"),
    AGE_10("10+"),
    AGE_12("12+"),
    AGE_14("14+"),
    AGE_16("16+"),
    AGE_18("18+");

    String age;

    PegiEnum(String age) {
        this.age = age;
    }
}
