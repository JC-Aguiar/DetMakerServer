package br.com.ppw.dma.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public abstract class SqlUtils {

    private static final Set<String> DDL_KEYWORDS = Set.of(
        "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "TRUNCATE", "RENAME", "DROP", "DECLARE"
    );

    private static final Set<String> JOIN_KEYWORDS = Set.of(
        "CROSS JOIN",
        "FULL JOIN",
        "RIGHT JOIN",
        "LEFT JOIN",
        "INNER JOIN",
        "NATURAL JOIN",
        "JOIN"
    );

    public enum DqlKeywords {
        SELECT(Set.of("SELECT")),
        FROM(Set.of("FROM")),
        JOIN(JOIN_KEYWORDS),
        WHERE(Set.of("WHERE")),
        ORDER(Set.of("ORDER")),
        GROUP(Set.of("GROUP")),
        HAVING(Set.of("HAVING")),
        LIMIT(Set.of("LIMIT")),
        OFFSET(Set.of("OFFSET")),
        FETCH(Set.of("FETCH")),
        UNION(Set.of("UNION")),
        INTERSECT(Set.of("INTERSECT")),
        MINUS(Set.of("MINUS")),
        WITH(Set.of("WITH")),
        SEARCH(Set.of("SEARCH")),
        CYCLE(Set.of("CYCLE")),
        ONLY(Set.of("ONLY")),
        ON(Set.of("ON"));

        @Getter public final Set<String> keywords;

        DqlKeywords(Set<String> keywords) {
            this.keywords = keywords;
        }
    }
//
//    public static final Set<String> DQL_KEYWORDS = Set.of(
//        "SELECT",
//        "FROM",
//        "WHERE",
//        "ORDER",
//        "GROUP",
//        "HAVING",
//        "LIMIT",
//        "OFFSET",
//        "FETCH",
//        "UNION",
//        "INTERSECT",
//        "MINUS",
//        "WITH",
//        "SEARCH",
//        "CYCLE",
//        "ONLY",
//        "ON"
//    );

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
        val keywords = DDL_KEYWORDS.stream()
            .filter(k -> !k.equals("INSERT"))
            .toList();
        String palavras = String.join("|", keywords);
        String ddlPattern = "(?i)(" +palavras+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafeDeleteQuery(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        val keywords = DDL_KEYWORDS.stream()
            .filter(k -> !k.equals("DELETE"))
            .toList();
        String palavras = String.join("|", keywords);
        String ddlPattern = "(?i)(" +palavras+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafeQuery(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        String palavras = String.join("|", DDL_KEYWORDS);
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

    public static String formatQuery(@NonNull String query) {
        return query
            .replaceAll("\r", " ")
            .replaceAll("\n", " ")
            .replaceAll("\t", " ")
//            .replaceAll("=", " = ")
//            .replaceAll("<>", " <> ")
//            .replaceAll("!=", " != ")
//            .replaceAll("<", " < ")
//            .replaceAll(">", " > ")
//            .replaceAll("<=", " <= ")
//            .replaceAll(">=", " >= ")
//            .replaceAll("\\.\\s+", ".")
            .replaceAll("\\s+", " ");
    }

    public static List<String> extractFromQuery(
        @NonNull String query,
        @NonNull DqlKeywords...keywords) {

        ArrayList<String> resultado = new ArrayList<>();
        query = formatQuery(query);
        log.info("Query: '{}'", query);
        log.info("DQLs: {}", Arrays.deepToString(keywords));
        if(keywords.length == 0) return List.of();

        // Listando todas as cláusulas DQL
        var clausulas = Arrays
            .stream(DqlKeywords.values())
            .parallel()
            .flatMap(keyword -> keyword.getKeywords().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // Mapeando cláusulas DQL do usuário
        var palavrasAlvo = Arrays.stream(keywords)
            .parallel()
            .flatMap(keyword -> keyword.getKeywords().stream())
            .collect(Collectors.toSet());

        // Gerando substring das cláusulas DQL a serem extraídas no regex
        var padraoAlvo = Arrays.stream(keywords)
            .parallel()
            .flatMap(keyword -> keyword.getKeywords().stream())
            .collect(Collectors.joining("|"));
        clausulas.removeAll(palavrasAlvo);

        // Gerando substring das cláusulas DQL a serem ignoradas no regex
        var padraoIgnorar = clausulas.parallelStream()
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));

        // Regex para extrair conteúdo entre as cláusulas DQL escolhidas x demais cláusulas DQL
        var regexLiteral = String.format("(?s)(?:%s)(?:(?!%s).)*", padraoAlvo, padraoIgnorar);
        log.info(regexLiteral.toString());
        var matcher = Pattern.compile(regexLiteral).matcher(query);

        // Itera sobre os conteúdos DQL encontrados
        while(matcher.find()) {
            val match = new StringBuilder(matcher.group());
            // Remove cláusula DQL do texto obtido (se existe)
            palavrasAlvo.forEach(palavra -> {
                int startIndex = match.indexOf(palavra);
                if (startIndex == -1) return ;
                int endIndex = startIndex + palavra.length();
                match.delete(startIndex, endIndex);
            });
            // Interpreta e mapeia resultado para tabelas
            resultado.add(match.toString());
        }
        return resultado;
    }

    private static Set<String> refineTableExtraction(@NonNull String tablesExtraction) {
        // Regex para identificar nome das tabelas + alias
        var pattern = Pattern.compile("(\\w+)(?:\\s+(\\w+))?");

        var tabelas = new HashSet<String>();
        String[] lines = tablesExtraction.split("\\n");
        for(String line : lines) {
            var matcher = pattern.matcher(line);
            while(matcher.find()) {
                String tableName = matcher.group(1);
                String alias = Optional.ofNullable(matcher.group(2)).orElse("");
                tabelas.add(tableName);
            }
        }
        return tabelas;
    }

    private static Set<String> refineColumnsExtraction(@NonNull String fieldsExtraction) {
        fieldsExtraction = fieldsExtraction
            .replaceAll("\\.\\s+", ".")
            .replaceAll(",", " ");
        return Arrays.stream(fieldsExtraction.split(" "))
            .filter(Predicate.not(String::isBlank))
            .map(line -> {
                int index = line.indexOf(".");
                if(index == -1) return line;
                return line.substring(index+1);
            })
            .map(FormatString::extrairConteudoParenteses)
            .collect(Collectors.toSet());
    }

    public static Set<String> getTablesNameFromQuery(@NonNull String query) {
        return SqlUtils.extractFromQuery(query, DqlKeywords.FROM, DqlKeywords.JOIN)
            .parallelStream()
            .map(SqlUtils::refineTableExtraction)
            .flatMap(Set::parallelStream)
            .collect(Collectors.toSet());
    }

    public static Set<String> getColumnsNameFromQuery(@NonNull String query) {
        return SqlUtils.extractFromQuery(query, DqlKeywords.SELECT)
            .parallelStream()
            .map(SqlUtils::refineColumnsExtraction)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

}
