package br.com.ppw.dma.util;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLSyntaxErrorException;
import java.util.List;

@Slf4j
public abstract class SqlUtils {

    private static final List<String> KEYWORDS = List.of(
        "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "TRUNCATE", "RENAME", "DROP", "DECLARE"
    );

//    public static Object parseToDate(@NonNull String s) {
//        return String.format(
//            "TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')", s
//        );
//    }
//
//    public static Object parseTotimestamp(@NonNull String s) {
//        return String.format(
//            "TO_TIMESTAMP('%s', 'DD/MM/YYYY HH24:MI:SS')", s
//        );
//    }
//
//    public static Object parseToDouble(@NonNull String s) {
//        return String.format(
//            "TO_TIMESTAMP('%s', 'DD/MM/YYYY HH24:MI:SS')", s
//        );
//    }

//    public static Object parseToFloat(@NonNull String s) {
//    }
//
//    public static Object parseToNumber(@NonNull String s) {
//    }

    //TODO: javadoc
    public static boolean isSafeInsertQuery(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        val keywords = KEYWORDS.stream()
            .filter(k -> !k.equals("INSERT"))
            .toList();
        String palavras = String.join("|", keywords);
        String ddlPattern = "(?i)(" +palavras+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafeDeleteQuery(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        val keywords = KEYWORDS.stream()
            .filter(k -> !k.equals("DELETE"))
            .toList();
        String palavras = String.join("|", keywords);
        String ddlPattern = "(?i)(" +palavras+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafeQuery(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        String palavras = String.join("|", KEYWORDS);
        String ddlPattern = "(?i)(" +palavras+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafeQuery(List<String> campos) {
        if(campos == null || campos.isEmpty()) return true;
        return campos.stream().allMatch(SqlUtils::isSafeQuery);
    }

    public static String getExceptionMainCause(@NonNull Exception e) {
        log.info("Tentando identificar a causa raiz por trás do exception SQL.");
        String mensagem = null;
        Throwable causa = e.getCause();

        while(causa != null) {
            log.debug("Causa: {}", causa.toString());
            if(causa instanceof SQLSyntaxErrorException) {
                log.debug("Causa raiz identificada.");
                mensagem = causa.getMessage();
                break;
            }
            causa = causa.getCause();
        }
        if(mensagem == null) {
            log.debug("Causa raiz não identificada.");
            mensagem = e.getMessage();
        }
        log.debug("Resultado: {}", mensagem);
        return mensagem;
    }
    
}
