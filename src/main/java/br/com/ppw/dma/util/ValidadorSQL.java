package br.com.ppw.dma.util;

import java.util.List;

public abstract class ValidadorSQL {
    
    private static final List<String> KEYWORDS = List.of(
        "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "TRUNCATE", "RENAME", "DROP"
    );

    //TODO: javadoc
    public static boolean isSafe(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        String palavras = String.join("|", KEYWORDS);
        String ddlPattern = "(?i)(" +palavras+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafe(List<String> campos) {
        if(campos == null || campos.isEmpty()) return true;
        return campos.stream().allMatch(ValidadorSQL::isSafe);
    }
    
}
