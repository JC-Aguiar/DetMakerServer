package br.com.ppw.dma.domain.master;

import br.com.ppw.dma.util.FormatString;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.sql.SQLSyntaxErrorException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.domain.master.SqlSintaxe.DqlKeywords.*;

@Slf4j
public abstract class SqlSintaxe {

    public enum DdlKeywords {
        INSERT, UPDATE, DELETE, CREATE, ALTER, TRUNCATE, RENAME, DROP, DECLARE
    };

    private static final List<String> JOIN_KEYWORDS = List.of(
        "CROSS JOIN",
        "FULL JOIN",
        "RIGHT JOIN",
        "LEFT JOIN",
        "INNER JOIN",
        "NATURAL JOIN",
        "JOIN"
    );

    private static final List<String> LOGIC_OPERATOR_KEYWORDS = List.of(
        "<>",
        "!=",
        "<=",
        ">=",
        "=",
        "<",
        ">",
        "NOT BETWEEN",
        "NOT LIKE",
        "NOT IN",
        "IS NOT NULL",
        "BETWEEN",
        "LIKE",
        "IN",
        "IS NULL"
    );

    private static final Set<String> COMBINE_OPERATOR_KEYWORDS = Set.of(
        "AND", "OR"
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
        EXIST(Set.of("EXIST")),
        CYCLE(Set.of("CYCLE")),
        ONLY(Set.of("ONLY")),
        ON(Set.of("ON")),
        COMBINE_OPERATORS(COMBINE_OPERATOR_KEYWORDS),
        LOGIC_OPERATORS(LOGIC_OPERATOR_KEYWORDS);

        @Getter public final Collection<String> keywords;

        DqlKeywords(Collection<String> keywords) {
            this.keywords = keywords;
        }
    }

    public enum QueryMethod {
        DDL("Data Definition Language"),
        //	DDL é usado para definir a estrutura dos objetos no banco de dados, como tabelas, índices, visões e esquemas. Exemplos de comandos DDL incluem CREATE, ALTER e DROP.
        DML("Data Manipulation Language"),
        //	DML é usado para manipular os dados dentro do banco de dados. Isso inclui operações como inserir, atualizar, excluir e recuperar dados.
        DQL("Data Query Language"),
        // DQL é usado para realizar consultas ao banco de dados.
        DCL("Data Control Language"),
        //	DCL é usado para gerenciar as permissões e privilégios no banco de dados. Comandos DCL incluem GRANT para conceder permissões e REVOKE para revogar permissões.
        TCL("Transaction Control Language");
        //	TCL é usado para gerenciar as transações no banco de dados. Comandos TCL incluem COMMIT para confirmar as alterações feitas durante uma transação e ROLLBACK para desfazer as alterações e restaurar o estado anterior.

        @Getter public final String fullName;

        QueryMethod(String fullName) {
            this.fullName = fullName;
        }
    }

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
    public static boolean isSafeInsert(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        val keywords = Stream.of(DdlKeywords.values())
            .filter(k -> k != DdlKeywords.INSERT)
            .map(Enum::name)
            .collect(Collectors.joining("|"));
        var ddlPattern = "(?i)(" +keywords+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafeDelete(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        val keywords = Stream.of(DdlKeywords.values())
            .filter(k -> k != DdlKeywords.DELETE)
            .map(Enum::name)
            .collect(Collectors.joining("|"));
        var ddlPattern = "(?i)(" +keywords+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public static boolean isSafeSelect(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        var palavras = Stream.of(DdlKeywords.values())
            .map(Enum::name)
            .collect(Collectors.joining("|"));
        var ddlPattern = "(?i)(" +palavras+ ")";
        return !query.matches(".*" + ddlPattern + ".*");
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
            .replaceAll("=", " = ")
            .replaceAll("<", " < ")
            .replaceAll(">", " > ")
            .replaceAll("<\\s+>", " <> ")
            .replaceAll("!\\s+=", " != ")
            .replaceAll("<\\s+=", " <= ")
            .replaceAll(">\\s+=", " >= ")
            .replaceAll("\\.\\s+", ".")
            .replaceAll("\\s+", " ");
    }

    private static List<String> extractFromQuery(
        @NonNull String query,
        @NonNull DqlKeywords...keywords) {

        return extractFromQuery(query, Set.of(keywords), Set.of());
    }

    private static List<String> extractFromQuery(
        @NonNull String query,
        @NonNull Set<DqlKeywords> keywordsInclude,
        @NonNull Set<DqlKeywords> keywordsExclude) {

        ArrayList<String> resultado = new ArrayList<>();
        if(keywordsInclude.size() == 0) return List.of();

        // Listando todas as cláusulas DQL
        var clausulas = Arrays.stream(DqlKeywords.values())
            .parallel()
            .flatMap(keyword -> keyword.getKeywords().stream())
            .collect(Collectors.toCollection(LinkedHashSet::new));

        // Mapeando cláusulas DQL do usuário
        var palavrasAlvo = keywordsInclude.parallelStream()
            .filter(keyword -> !keywordsExclude.contains(keyword))
            .flatMap(keyword -> keyword.getKeywords().stream())
            .toList();

        // Mapeando cláusulas DQL do usuário
        var palavrasIgnorar = keywordsExclude.parallelStream()
            .filter(keyword -> !keywordsInclude.contains(keyword))
            .flatMap(keyword -> keyword.getKeywords().stream())
            .toList();

        // Removendo das demais cláusulas as escolhidas
        palavrasAlvo.forEach(clausulas::remove);
        palavrasIgnorar.forEach(clausulas::remove);

        // Gerando substring das cláusulas DQL a serem extraídas no regex
        var padraoAlvo = keywordsInclude.parallelStream()
            .flatMap(keyword -> keyword.getKeywords().stream())
            .map(keyword -> " "+keyword+" ")
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));

        // Gerando substring das cláusulas DQL a serem ignoradas no regex
        var padraoIgnorar = clausulas.parallelStream()
            .map(keyword -> " "+keyword+" ")
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));

        // Regex para extrair conteúdo entre as cláusulas DQL escolhidas x demais cláusulas DQL
        var regexLiteral = String.format("(?s)(?:%s)(?:(?!%s).)*", padraoAlvo, padraoIgnorar);
        var matcher = Pattern.compile(regexLiteral).matcher(query);

        // Itera sobre os conteúdos para remover os DQL encontrados na extração
        var palavrasRefinar = new ArrayList<>(palavrasAlvo);
        palavrasRefinar.addAll(palavrasIgnorar);

        while(matcher.find()) {
            val text = new StringBuilder(matcher.group());
            // Remove cláusula DQL do texto obtido (se existe)
            for(var palavra : palavrasRefinar) {
                int startIndex = text.indexOf(palavra);
                while(startIndex != -1) {
                    int endIndex = startIndex + palavra.length();
                    text.delete(startIndex, endIndex);
                    startIndex = text.indexOf(palavra);
                }
            }
            // Interpreta e mapeia resultado para tabelas
            resultado.add(text.toString());
        }
        return resultado;
    }

    private static Set<String> refineTablesExtraction(@NonNull String tablesExtraction) {
        // Regex para identificar name das tabelas + alias
        var pattern = Pattern.compile("(\\w+)(?:\\s+(\\w+))?");

        var tables = new HashSet<String>();
        String[] lines = tablesExtraction.split("\\n");
        for(String line : lines) {
            var matcher = pattern.matcher(line);
            while(matcher.find()) {
                String tableName = FormatString.extrairConteudoParenteses(matcher.group(1));
                String alias = Optional.ofNullable(matcher.group(2)).orElse("");
                tables.add(tableName);
            }
        }
        return tables;
    }

    private static Set<String> refineColumnsExtraction(@NonNull String fieldsExtraction) {
        if(fieldsExtraction.contains("*")) return Set.of();

        fieldsExtraction = fieldsExtraction.replaceAll(",", " ");
        return Arrays.stream(fieldsExtraction.split(" "))
            .parallel()
            .filter(Predicate.not(String::isBlank))
            .map(line -> {
                int index = line.indexOf(".");
                if(index == -1) return line;
                return line.substring(index+1);
            })
            .map(FormatString::extrairConteudoParenteses)
            .collect(Collectors.toSet());
    }

    private static List<QueryFilter> refineFiltersExtraction(@NonNull String filtersExtraction) {
        var results = new ArrayList<QueryFilter>();
        var filtersExtractionArray = filtersExtraction
            .replaceAll(",\\s+", ",")
            .replaceAll("\\s+", " ")
            .trim()
            .split(" ");
        for(int index = 0; index < filtersExtractionArray.length; index++) {
            var column = filtersExtractionArray[index];
            var columnName = refineColumnName(column);
            var invalid = columnName.isBlank()
                || FormatString.possuiVariaveis(column)
                || isUserInput(column)
                || isUserInput(columnName);
            if(invalid) continue;

            var thisResult = results.parallelStream()
                .filter(queryCol -> queryCol.column().equals(columnName))
                .findFirst()
                .orElseGet(() -> new QueryFilter(columnName, new HashSet<>()));

            if(index+1 < filtersExtractionArray.length) {
                var nextItem = filtersExtractionArray[index+1];
                if(FormatString.possuiVariaveis(nextItem)) {
                    index++;
                    var array = nextItem.startsWith("(") && nextItem.endsWith(")");
                    var variable = FormatString.extrairVariaveisLista(nextItem).get(0);
                    thisResult.variables().add(
                        new QueryVariable(variable, array)
                    );
                }
            }
            if(results.parallelStream().noneMatch(queryCol -> queryCol.column().equals(columnName)))
                results.add(thisResult);
        }
        return results;
    }

    private static boolean isUserInput(@NonNull String text) {
        var regex = "^(?:'[^']*'|[0-9]+)$";
        return Pattern.compile(regex)
            .matcher(text)
            .find();
    }

    private static String refineColumnName(@NonNull String column) {
        var builder = new StringBuilder(
            FormatString.extrairConteudoParenteses(column)
        );
        var index = builder.lastIndexOf(".");
        if(index != -1) builder.delete(0, index+1);

        index = builder.indexOf(",");
        if(index == -1) return builder.toString();

        return builder
            .delete(index, builder.length())
            .toString();
    }

    private static Set<String> getTablesNameFromQuery(@NonNull String query) {
        return Set.copyOf(SqlSintaxe.extractFromQuery(query, FROM, JOIN))
            .parallelStream()
            .map(SqlSintaxe::refineTablesExtraction)
            .flatMap(Set::parallelStream)
            .collect(Collectors.toSet());
    }

    private static Set<String> getColumnsNameFromQuery(@NonNull String query) {
        return Set.copyOf(SqlSintaxe.extractFromQuery(query, SELECT))
            .parallelStream()
            .map(SqlSintaxe::refineColumnsExtraction)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    private static List<QueryFilter> getColumnsFiltersFromQuery(@NonNull String query) {
        var extraction = Set.copyOf(SqlSintaxe.extractFromQuery(
                query,
                Set.of(WHERE, ON, COMBINE_OPERATORS),
                Set.of(LOGIC_OPERATORS)))
            .parallelStream()
            .collect(Collectors.joining(" "));
        return refineFiltersExtraction(extraction);
    }

    public static QueryExtraction analyse(String query) {
        log.info("Analisando query...");
        if(query == null) return QueryExtraction.empty();

        query = formatQuery(query);
        var tables = SqlSintaxe.getTablesNameFromQuery(query);
        var columns = SqlSintaxe.getColumnsNameFromQuery(query);
        var filters = SqlSintaxe.getColumnsFiltersFromQuery(query);
        return new QueryExtraction(tables, columns, filters);
    }


}
