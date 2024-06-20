package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.util.TipoColuna;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FiltroTipo {
//    CHAR("char", Character.class, TipoColuna.COLUNAS_TEXTUAIS),
    UNSET("", List.of()),
    STRING("string", TipoColuna.COLUNAS_TEXTUAIS),
    NUMBER("number", TipoColuna.COLUNAS_NUMERICAS),
//    DECIMAL("decimal", BigDecimal.class, TipoColuna.COLUNAS_TEXTUAIS),
    DATE("date", TipoColuna.COLUNAS_DATAVEIS),
//    CHAR_ARRAY("char[]", Character.class, TipoColuna.COLUNAS_TEXTUAIS),
    STRING_ARRAY("string[]", TipoColuna.COLUNAS_TEXTUAIS),
    NUMBER_ARRAY("number[]", TipoColuna.COLUNAS_NUMERICAS),
//    DECIMAL_ARRAY("decimal[]", BigDecimal.class, TipoColuna.COLUNAS_TEXTUAIS);
//    DATE_ARRAY("date[]")
    ;

    public final String nome;
    public final List<TipoColuna> tiposSuportados;

    FiltroTipo(String nome, List<TipoColuna> colunas) {
        this.nome = nome;
        this.tiposSuportados = colunas;
    }


    private static final String NAO_IDENTIFICADO = "O tipo informado '%s' para o FitlroSql não " +
        "corresponde a nenhuma das opções registradas no DET-MAKER: " + getTodosOsTiposComoString();

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
            .orElse(UNSET);
//            .orElseThrow(() -> new RuntimeException(mensagemErro(texto)));

        if(!texto.trim().endsWith("[]")) return filtroTipo;

        filtroTipo = switch(filtroTipo) {
//            case CHAR -> CHAR_ARRAY;
            case STRING -> STRING_ARRAY;
            case NUMBER -> NUMBER_ARRAY;
//            case DECIMAL -> DECIMAL_ARRAY;
//            case DATE -> DATE_ARRAY;
            default -> UNSET;
        };
//        if(filtroTipo == null) throw new RuntimeException(mensagemErro(texto));
        return filtroTipo;
    }

    //TODO: javadoc
    public static String formatarValor(@NonNull FiltroTipo tipo, @NonNull String valor) {
        if(valor.trim().isEmpty()) throw new RuntimeException(mensagemErro(valor));
        return switch(tipo) {
//            case CHAR -> formatarChar(valor);
            case STRING, DATE -> formatarString(valor);
            case NUMBER -> String.valueOf(Long.parseLong(valor));
//            case DECIMAL -> String.valueOf(Double.parseDouble(valor));
            //case DATE, DATE_ARRAY -> valor; //TODO: criar validador entre padrão e input
//            case CHAR_ARRAY -> formatarCharArray(valor);
            case STRING_ARRAY -> formatarStringArray(valor); //DATE_ARRAY
            case NUMBER_ARRAY -> formatarNumberArray(valor);
//            case DECIMAL_ARRAY -> formatarDecimalArray(valor);
            default -> ""; //TODO: lançar exceção?
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
