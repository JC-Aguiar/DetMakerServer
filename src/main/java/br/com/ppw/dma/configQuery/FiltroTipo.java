package br.com.ppw.dma.configQuery;

import lombok.NonNull;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FiltroTipo {
    CHAR("char", Character.class),
    STRING("string", String.class),
    NUMBER("number", Long.class),
    DECIMAL("decimal", BigDecimal.class),
    DATE("date", OffsetDateTime.class),
    CHAR_ARRAY("char[]", Character.class),
    STRING_ARRAY("string[]", String.class),
    NUMBER_ARRAY("number[]", Long.class),
    DECIMAL_ARRAY("decimal[]", BigDecimal.class);
//    DATE_ARRAY("date[]");

    public final String nome;

    private static final String NAO_IDENTIFICADO = "O tipo informado '%s' para o FitlroSql não " +
        "corresponde a nenhuma das opções registradas no DET-MAKER: " + getTodosOsTiposComoString();

    FiltroTipo(String nome, Class<?> classe) {
        this.nome = nome;

    }

    public static String getTodosOsTiposComoString() {
        return Arrays.stream(FiltroTipo.values())
            .map(ft -> ft.nome)
            .collect(Collectors.joining(", "));
    }

    private static String mensagemErro(@NonNull String sqlTipo) {
        return String.format(NAO_IDENTIFICADO, sqlTipo);
    }

    //TODO: javadoc
    public static FiltroTipo identificar(@NonNull String texto) {
        //TODO: criar exception própria
        var filtroTipo = Arrays.stream(FiltroTipo.values())
            .filter(ft -> texto.trim().toUpperCase().startsWith(ft.nome.toUpperCase()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(mensagemErro(texto)));

        if(!texto.trim().endsWith("[]")) return filtroTipo;

        filtroTipo = switch(filtroTipo) {
            case CHAR -> CHAR_ARRAY;
            case STRING -> STRING_ARRAY;
            case NUMBER -> NUMBER_ARRAY;
            case DECIMAL -> DECIMAL_ARRAY;
//            case DATE -> DATE_ARRAY;
            default -> null;
        };
        if(filtroTipo == null) throw new RuntimeException(mensagemErro(texto));
        return filtroTipo;
    }

    //TODO: javadoc
    public static String formatarValor(@NonNull FiltroTipo tipo, @NonNull String valor) {
        if(valor.trim().isEmpty()) throw new RuntimeException(mensagemErro(valor));
        return switch(tipo) {
            case CHAR -> formatarChar(valor);
            case STRING, DATE -> formatarString(valor);
            case NUMBER -> String.valueOf(Long.parseLong(valor));
            case DECIMAL -> String.valueOf(Double.parseDouble(valor));
            //case DATE, DATE_ARRAY -> valor; //TODO: criar validador entre padrão e input
            case CHAR_ARRAY -> formatarCharArray(valor);
            case STRING_ARRAY -> formatarStringArray(valor); //DATE_ARRAY
            case NUMBER_ARRAY -> formatarNumberArray(valor);
            case DECIMAL_ARRAY -> formatarDecimalArray(valor);
        };
    }

    //TODO: javadoc
    public static String identificarFormatar(@NonNull String tipoSql, @NonNull String valor) {
        return formatarValor(identificar(tipoSql), valor);
    }

    private static String formatarChar(@NonNull String valor) {
        if (valor.contains("'")) valor = valor.replace("'", "''");
        return "'" + valor.trim().charAt(0) + "'";
    }

    private static String formatarString(@NonNull String valor) {
        if (valor.contains("'")) valor = valor.replace("'", "''");
        return "'" + valor + "'";
    }

    private static Stream<String> dividirArray(@NonNull String valor) {
        valor = valor.replace(",", ";");
        return Arrays.stream(valor.split(";"));
    }

    private static String formatarCharArray(@NonNull String valor) {
        return dividirArray(valor)
            .map(FiltroTipo::formatarChar)
            .collect(Collectors.joining(", "));
    }

    private static String formatarStringArray(@NonNull String valor) {
        return dividirArray(valor)
            .map(FiltroTipo::formatarString)
            .collect(Collectors.joining(", "));
    }

    private static String formatarNumberArray(@NonNull String valor) {
        dividirArray(valor).forEach(Long::parseLong);
        return valor;
    }

    private static String formatarDecimalArray(@NonNull String valor) {
        //TODO: aprimorar
        dividirArray(valor).forEach(Double::parseDouble);
        return valor;
    }

}
