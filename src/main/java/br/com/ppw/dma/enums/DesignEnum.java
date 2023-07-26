package br.com.ppw.dma.enums;

public enum DesignEnum {
    PB("Black & White"),
    COLLOR("Collor"),
    LIVE_ACTION("Live Action"),
    STOP_MOTION("Stop Motion"),
    CARTOON("Cartoon"),
    CG("Computer Graphics");

    String style;

    DesignEnum(String style) {
        this.style = style;
    }
}
