package br.com.ppw.dma.configQuery;

import lombok.NonNull;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum FiltroTipo {
    CHAR("char", 5),
    STRING("string", 6),
    NUMBER("number", 7),
    DECIMAL("decimal", 8),
    DATE("date", 9),
    CHAR_ARRAY("char[]", -1),
    STRING_ARRAY("string[]", -1),
    NUMBER_ARRAY("number[]", -1),
    DECIMAL_ARRAY("decimal[]", -1),
    DATE_ARRAY("date[]", -1);

    public final String nome;
    public final int arrayRef;

    private static final String NAO_IDENTIFICADO = "O tipo informado '%s' para o FitlroSql não " +
        "corresponde a nenhuma das opções registradas no DET-MAKER: " + getTodosOsTiposComoString();

    FiltroTipo(String nome, int arrayRef) {
        this.nome = nome;
        this.arrayRef = arrayRef;
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
    public static FiltroTipo identificar(@NonNull String sqlTipo) {
        //TODO: criar exception própria
        val filtroTipo = Arrays.stream(FiltroTipo.values())
            .filter(ft -> sqlTipo.trim().startsWith(ft.nome))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(mensagemErro(sqlTipo)));

        if (sqlTipo.trim().endsWith("[]") && filtroTipo.arrayRef != -1)
            return FiltroTipo.values()[filtroTipo.arrayRef];
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
            case STRING_ARRAY, DATE_ARRAY -> formatarStringArray(valor);
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
