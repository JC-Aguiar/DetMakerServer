package br.com.ppw.dma.util;

import java.util.List;

public abstract class ValidadorSQL {
    
    private static final List<String> KEYWORDS = List.of(
        "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP"
    );
    
    public static boolean querySegura(String query) {
        return KEYWORDS.stream().noneMatch(key -> query.toUpperCase().contains(key));
    }
    
}
