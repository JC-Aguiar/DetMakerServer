package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.util.FormatString;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Setter
@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FiltroSql {

    String coluna;
    String valor = "";
    String tipo;

    public FiltroSql(String coluna, String tipo) {
        this.coluna = coluna;
        this.tipo = tipo;
    }

    //TODO: javadoc
    public static List<FiltroSql> identificar(@NotBlank String texto) {
        val variaveis = FormatString.obterVariaveis(texto);
        String chave = null;
        val filtros = new ArrayList<FiltroSql>();
        for(int i = 0; i < variaveis.size(); i++) {
            if(i % 2 == 0) {
                chave = variaveis.get(i);
            }
            else {
                String valor = variaveis.get(i);
                filtros.add(new FiltroSql(chave, valor));
            }
        }
        return filtros;
    }

    //TODO: javadoc
    public static String montarSql(@NotBlank String sql, List<FiltroSql> filtros) {
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
                val valor = FiltroTipo.identificarFormatar(filtroSql.tipo, filtroSql.valor);
                matcher.appendReplacement(outputStringBuffer, valor);
            }
        }
        matcher.appendTail(outputStringBuffer);

        final String sqlFinal = outputStringBuffer.toString();
        return sqlFinal;
    }

}
