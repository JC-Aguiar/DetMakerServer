package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.util.FormatString;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FiltroSql {

    String coluna;
    String valor = "";
    String nome;
    //FiltroTipo tipo;
    boolean array;

    public static final LinkedHashSet<String> OPERADORES_REMOVER = new LinkedHashSet<>();


    static {
        OPERADORES_REMOVER.add("=");
        OPERADORES_REMOVER.add("<>");
        OPERADORES_REMOVER.add("!=");
        OPERADORES_REMOVER.add("<");
        OPERADORES_REMOVER.add(">");
        OPERADORES_REMOVER.add("<=");
        OPERADORES_REMOVER.add(">=");
        OPERADORES_REMOVER.add("IN");
        OPERADORES_REMOVER.add("NOT IN");
        OPERADORES_REMOVER.add("BETWEEN");
        OPERADORES_REMOVER.add("NOT BETWEEN");
        OPERADORES_REMOVER.add("LIKE");
        OPERADORES_REMOVER.add("NOT LIKE");
    };

    public FiltroSql(@NonNull String coluna, @NonNull String nome, boolean array) {
        this.coluna = coluna;
        this.nome = nome;
//        this.tipo = tipo;
        this.array = array;
    }

    //TODO: javadoc
    //TODO: criar throw próprio
    public static List<FiltroSql> identificar(@NonNull String query) {
        val filtros = new ArrayList<FiltroSql>();
        var novaQuery = limparQuery(query);
//        if(novaQuery.contains("{") || novaQuery.contains("}")) {
//            throw new RuntimeException(
//                "Configuração de Query template inválida. "
//                + "Provável espaço em branco dentro do nome da variável"
//            );
//        }
        var linhas = novaQuery.split("\\s+");
//        var mapaMetadados = new HashMap<String, FiltroTipo>();
//        for(var dados : metadados.split(",")) {
//            if(!dados.contains("=")) continue;
//            var campoValor = dados.split("=");
//            mapaMetadados.put(
//                campoValor[0].trim(),
//                FiltroTipo.identificar(campoValor[1])
//            );
//        }
        for(var i = 1; i < linhas.length; i++) {
            if(!textoPossuiVariavel(linhas[i])) continue;
            var array = variavelArray(linhas[i]);
            var variavelNome = extrairNomeVariavel(linhas[i]);
//            if(!mapaMetadados.containsKey(variavelNome)) continue;
//            var variavelTipo = mapaMetadados.remove(variavelNome);

            filtros.add(new FiltroSql(
                FormatString.extrairConteudoParenteses(linhas[i-1]),
                variavelNome,
                array
//                variavelTipo
            ));
        }
        /*
        val variaveis = FormatString.extrairVariaveisLista(texto);
        String chave = null;
        for(int i = 0; i < variaveis.size(); i++) {
            if(i % 2 == 0) {
                chave = variaveis.get(i);
            }
            else {
                String valor = variaveis.get(i);
                filtros.add(new FiltroSql(chave, valor));
            }
        }
        */
        return filtros;
    }

    private static String limparQuery(@NonNull String query) {
        var novaQuery = new StringBuilder(query.replace("=", " = "));
        OPERADORES_REMOVER.forEach(operador -> {
            int index;
            while ((index = novaQuery.indexOf(operador)) != -1) {
                novaQuery.delete(index, index + operador.length());
            }
        });
        return novaQuery.toString();
    }

    private static boolean textoPossuiVariavel(@NonNull String txt) {
        return txt.contains("${") && txt.contains("}");
    }

    private static boolean variavelArray(@NonNull String txt) {
        return txt.startsWith("(") && txt.endsWith(")");
    }

    private static String extrairNomeVariavel(@NonNull String txt) {
        return FormatString.extrairConteudoParenteses(
                FormatString.extrairVariaveis(txt)
        );
//        int start = builder.indexOf("${");
//        int end = builder.indexOf("}");
//        while(start != -1 && end != -1) {
//            builder.delete(start, start+2);
//            builder.deleteCharAt(end-2);
//            start = builder.indexOf("${");
//            end = builder.indexOf("}");
//        }

    }

    //TODO: javadoc
    public static String montarSql(@NotBlank String sql, List<FiltroSql> filtros) {
        StringBuffer queryFinal = new StringBuffer();
        for(var linha : sql.split("\\s+")) {
            queryFinal
                .append(extrairNomeVariavel(linha))
                .append(" ");
        }
        return queryFinal.toString();
        /*
        Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}"); //old regex: "\\$\\{([^}]+)\\}"
        Matcher matcher = pattern.matcher(sql);
        StringBuffer outputStringBuffer = new StringBuffer();
        int matchCount = -1;

        while(matcher.find()) {
            matchCount += 1;
            val index = matchCount / 2;
            if(index >= filtros.size()) break;

            val filtroSql = filtros.get(index);
            if(matchCount % 2 == 0)
                matcher.appendReplacement(outputStringBuffer, filtroSql.coluna);
            else {
                val valor = FiltroTipo.identificarFormatar(filtroSql.nome, filtroSql.valor);
                matcher.appendReplacement(outputStringBuffer, valor);
            }
        }
        matcher.appendTail(outputStringBuffer);
        final String sqlFinal = outputStringBuffer.toString();
        return sqlFinal;
        */
    }

}
