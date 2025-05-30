package br.com.ppw.dma.util;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class FormatDate {

    public static final Clock RELOGIO = Clock.tick(Clock.systemDefaultZone(), Duration.ofMillis(1));
    public static final DateTimeFormatter FORMAL_STYLE = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    public static final DateTimeFormatter BRASIL_STYLE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter FILENAME_STYLE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final DateTimeFormatter SQL_INTERNATIONAL_STYLE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter SQL_BRASIL_STYLE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter SQL_EUA_STYLE = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    public static final DateTimeFormatter BASH_PARAMETER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String formalStyle() {
        return LocalDateTime.now(RELOGIO).format(FORMAL_STYLE);
    }

    public static String brasilStyle() {
        return LocalDateTime.now(RELOGIO).format(BRASIL_STYLE);
    }

    public static String fileNameStyle() {
        return LocalDateTime.now(RELOGIO).format(FILENAME_STYLE);
    }

}
