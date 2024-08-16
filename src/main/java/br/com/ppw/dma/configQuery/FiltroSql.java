package br.com.ppw.dma.configQuery;

import br.com.ppware.api.TipoColuna;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Valid
@Setter
@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FiltroSql implements Serializable {

    @Nullable
    Long id;

    @NotBlank
    String tabela;

    @NotBlank
    String coluna;

    @NotBlank
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    TipoColuna tipo;

    @NotNull @Min(0)
    Integer index;

    @NotNull
    Boolean array;

    @NotBlank
    String variavel;

    @Nullable
    ColumnInfo metaDados;


    public static final List<String> OPERADORES_REMOVER = List.of(
        "=",
        "<>",
        "!=",
        "<",
        ">",
        "<=",
        ">=",
        " NOT IN",
        " NOT BETWEEN",
        " NOT LIKE",
        " IN ",
        " BETWEEN ",
        " LIKE "
    );

//    public FiltroSql(String coluna, String nome, boolean array, int index) {
//        this.coluna = coluna;
//        this.nome = nome;
////        this.tipo = tipo;
//        this.array = array;
//        this.index = index;
//    }

    public FiltroSql(@NonNull ConfigQueryVar queryVar) {
        this.id = queryVar.getId();
        this.coluna = queryVar.getColuna();
        this.tipo = queryVar.getTipo();
        this.variavel = queryVar.getNome();
        this.index = queryVar.getIndex();
        this.array = queryVar.getArray();
        this.metaDados = new ColumnInfo(
            queryVar.getTamanho(),
            queryVar.getPrecisao(),
            queryVar.getEscala()
        );
    }

    public String gerarValorAleatorio() {
        var valor = tipo.valorAleatorio(metaDados);
        if(!array) return valor;
        return valor + ", " + tipo.valorAleatorio(metaDados);
    }

    //TODO: javadoc
    //TODO: criar throw próprio
    public static List<FiltroSql> identificar(@NonNull String query) {
        val filtros = new ArrayList<FiltroSql>();
        var novaQuery = formatQuery(query);
        System.out.println("NOVA QUERY: " + novaQuery);
        var whereParametros = getWhereParameters(novaQuery);
        var regexArray = "\\((.*?)\\)";
        for(int index = 0; index < whereParametros.size(); index++) {
            if(index < 1) continue;
            var coluna = whereParametros.get(index-1);
            if(coluna.contains("(") && coluna.contains(")")) {
                Pattern pattern = Pattern.compile(regexArray);
                Matcher matcher = pattern.matcher(coluna);
                if(matcher.find()) coluna = matcher.group(1);
            }
            var valor = whereParametros.get(index);
            var array = valor.startsWith("(") && valor.endsWith(")");
            var filtro = new FiltroSql();
            filtro.setColuna(coluna);
            filtro.setVariavel(valor);
            filtro.setArray(array);
            filtros.add(filtro);
        }
        return filtros;
/*
        tabelas:
        FROM(.*?)(?:\sORDER BY|\sWHERE|\sGROUP BY|\sHAVING|\sLIMIT|\sOFFSET|\sFETCH|\sSELECT|\s$)
 */
    }

    private static String formatQuery(@NonNull String query) {
        var novaQuery = new StringBuilder(
            query.replaceAll("\r", " ")
                .replaceAll("\n", " ")
                .replaceAll("\t", " ")
                .replaceAll("=", " = ")
                .replaceAll("<>", " <> ")
                .replaceAll("!=", " != ")
                .replaceAll("<", " < ")
                .replaceAll(">", " > ")
                .replaceAll("<=", " <= ")
                .replaceAll(">=", " >= ")
                .replaceAll("\\s+", " "));
        OPERADORES_REMOVER.forEach(operador -> {
            int index;
            while ((index = novaQuery.indexOf(operador)) != -1) {
                novaQuery.delete(index, index + operador.length());
            }
        });
        return novaQuery.toString();
    }

    private static List<String> getWhereParameters(@NonNull String query) {
        var regex = "WHERE(.*?)" +
            "(?:\\sORDER BY|\\sGROUP BY|\\sHAVING|\\sLIMIT|\\sOFFSET|\\sFETCH|\\sSELECT|\\s$)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(query);

        List<String> whereMatches = new ArrayList<>();
        while (matcher.find()) {
            var whereValue = matcher.group(1);
            System.out.println("WHERE: " + whereValue);
            String[] whereMatch = whereValue.split(" ");
            Arrays.stream(whereMatch)
                .parallel()
                .map(String::trim)
                .filter(trecho -> !OPERADORES_REMOVER.contains(trecho))
                .filter(trecho -> !trecho.isBlank())
                .forEach(whereMatches::add);
        }
        System.out.println("NOVO WHERE:");
        whereMatches.forEach(System.out::println);
        return whereMatches;
    }

//    private static boolean textoPossuiVariavel(@NonNull String txt) {
//        return txt.contains("${") && txt.contains("}");
//    }
//
//    private static boolean variavelArray(@NonNull String txt) {
//        return txt.startsWith("(") && txt.endsWith(")");
//    }

}
