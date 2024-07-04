package br.com.ppw.dma.configQuery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.io.Serializable;

@Valid
@Setter
@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FiltroSql implements Serializable {

    //TODO: sincronizar com front
    Long id;
    @NotBlank String tabela;
    @NotBlank String coluna;
    @NotBlank String tipo;
    @NotNull @Min(0) Integer index;
    @NotNull Boolean array;
    @NotBlank String variavel;

    @Nullable @JsonIgnore ColumnInfo metaDados;
    @JsonIgnore String valor = "";

//    public static final LinkedHashSet<String> OPERADORES_REMOVER = new LinkedHashSet<>();
//
//
//    static {
//        OPERADORES_REMOVER.add("=");
//        OPERADORES_REMOVER.add("<>");
//        OPERADORES_REMOVER.add("!=");
//        OPERADORES_REMOVER.add("<");
//        OPERADORES_REMOVER.add(">");
//        OPERADORES_REMOVER.add("<=");
//        OPERADORES_REMOVER.add(">=");
//        OPERADORES_REMOVER.add("IN");
//        OPERADORES_REMOVER.add("NOT IN");
//        OPERADORES_REMOVER.add("BETWEEN");
//        OPERADORES_REMOVER.add("NOT BETWEEN");
//        OPERADORES_REMOVER.add("LIKE");
//        OPERADORES_REMOVER.add("NOT LIKE");
//    };

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
        this.tipo = queryVar.getTipo().name();
        this.variavel = queryVar.getNome();
        this.index = queryVar.getIndex();
        this.array = queryVar.getArray();
        this.metaDados = new ColumnInfo(
            queryVar.getTamanho(),
            queryVar.getPrecisao(),
            queryVar.getEscala()
        );
    }

    //TODO: javadoc
    //TODO: criar throw próprio
//    public static List<FiltroSql> identificar(@NonNull String query) {
//        val filtros = new ArrayList<FiltroSql>();
//        var novaQuery = limparQuery(query);
//        var linhas = novaQuery.split("\\s+");
//        for(var i = 1; i < linhas.length; i++) {
//            if(!textoPossuiVariavel(linhas[i])) continue;
//            var array = variavelArray(linhas[i]);
//            var variavelNome = extrairNomeVariavel(linhas[i]);
//
//            filtros.add(new FiltroSql(
//                FormatString.extrairConteudoParenteses(linhas[i-1]),
//                variavelNome,
//                array,
//                i
//            ));
//        }
//        return filtros;
//    }

//    private static String limparQuery(@NonNull String query) {
//        var novaQuery = new StringBuilder(query.replace("=", " = "));
//        OPERADORES_REMOVER.forEach(operador -> {
//            int index;
//            while ((index = novaQuery.indexOf(operador)) != -1) {
//                novaQuery.delete(index, index + operador.length());
//            }
//        });
//        return novaQuery.toString();
//    }

//    private static boolean textoPossuiVariavel(@NonNull String txt) {
//        return txt.contains("${") && txt.contains("}");
//    }
//
//    private static boolean variavelArray(@NonNull String txt) {
//        return txt.startsWith("(") && txt.endsWith(")");
//    }

}
