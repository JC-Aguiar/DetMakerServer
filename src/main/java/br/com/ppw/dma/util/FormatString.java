package br.com.ppw.dma.util;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.val;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FormatString {

    public static final String LINHA_HORINZONTAL = "******************************";
    public static final String LINHA_HIFENS = "------------------------------";
    static final String SEPARADOR_TABELA_COLUNA = " | ";
    static final char SEPARADOR_TABELA_HEADER = '-';
    public static final List<String> INDICADORES_QUEBRA_LINHA = Arrays.asList("\n", ";");
    public static final List<String> INDICADORES_NULOS = Arrays.asList(
        "N/A", "VAZIO", "NULL", "NUL", "NULO","NÃO", "NAO", "NO", "NENHUM", "NADA", "NENHUN", "0", "-", "X"
    );

    /**
     * Verifica se o texto obtido indica representar um valor nulo (exemplo: "N/A").
     * Os indicadores de valor nulo se encontram na variável {@link FormatString#INDICADORES_NULOS}.
     * @param texto {@link String} texto a validar
     * @return true se indica vazio/nulo ou false se o texto de fato tem um conteúdo importante.
     */
    public static String valorVazio(String texto) {
        if(texto == null || texto.isEmpty()) return "";

        val textoRefinado = refinarCelula(texto);
        val textoSemConteudo = INDICADORES_NULOS
            .stream()
            .anyMatch(ind -> ind.equalsIgnoreCase(textoRefinado));
        return textoSemConteudo ? "" : textoRefinado;
    }

    /**
     * Método destinado a tratar textos retirados de células Excel ou de outros editores de texto que
     * tendem a adicionar caracteres especiais, como aspas simples ou aspas duplas.
     * @param texto {@link String} a ser refinada
     * @return {@link String} refinada
     */
    public static String refinarCelula(String texto) {
        return texto.replace("'", "")
            .replace("[", "")
            .replace("]", "")
            .replace("\"", "")
            .trim();
    }

    /**
     * Método para tratar e formar textos, a fim de estarem compatíveis aos padrões de nomes de arquivo para
     * Windows e Linux
     * @param input {@link String}
     * @return {@link String} texto formatado
     */
    public static String nomeParaArquivo(String input) {
        //Remove caracteres inválidos para nomes de arquivo
        String textoFormatado = input.replaceAll("[\\\\/:*?\"<>|]", "");

        //Limita o tamanho máximo do name do arquivo para 255 caracteres (limite do Windows)
        int maxFileNameLength = 255;
        if(textoFormatado.length() > maxFileNameLength)
            textoFormatado = textoFormatado.substring(0, maxFileNameLength);

        //Remove pontos finais ou espaços em branco no final do name do arquivo
        return textoFormatado.replaceAll("[.\\s]+$", "")
            .replace(" ", "_");
    }

    public static String refinarTextoJavascript(String texto) {
        if(texto == null) return "``";
        return texto.replace("`", "\\`")
            .replace("\\", "\\\\`")
            .trim();
    }

    public static List<String> extrairVariaveisLista(@NotBlank String texto) {
        String regex = "\\$\\{(.*?)\\}";
        val resultado = new ArrayList<String>();
        val pattern = Pattern.compile(regex);
        val matcher = pattern.matcher(texto);
        while(matcher.find()) {
            resultado.add(matcher.group(1));
        }
        return resultado;
    }

    public static boolean possuiVariaveis(String input) {
        var pattern = Pattern.compile("\\$\\{(.*?)\\}");
        var matcher = pattern.matcher(input);
        return matcher.find();
    }

    public static String substituirVariaveis(String input, String signal) {
        var pattern = Pattern.compile("\\$\\{(.*?)\\}");
        var matcher = pattern.matcher(input);
        var output = new StringBuffer();

        while(matcher.find()) {
            String variable = matcher.group(1);
            String replacement = Matcher.quoteReplacement(signal);
            matcher.appendReplacement(output, replacement);
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public static String substituirVariaveis(
        @NonNull String input,
        @NonNull Map<String, String> variables) {

        var pattern = Pattern.compile("\\$\\{(.*?)\\}"); // "\\$\\{([^}]+)}"
        var matcher = pattern.matcher(input);
        var output = new StringBuffer();

        while(matcher.find()) {
            String variable = matcher.group(1);
            String value = variables.get(variable);
            String replacement = (value != null) ? Matcher.quoteReplacement(value) : "";
            matcher.appendReplacement(output, replacement);
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public static String substituirVariaveis(
            @NonNull String input,
            @NonNull Map<String, String> variables,
            @NonNull String regex) {

        var matcher = Pattern.compile(regex).matcher(input);
        var output = new StringBuffer();

        while(matcher.find()) {
            String variable = matcher.group(1);
            String value = variables.get(variable);
            String replacement = (value != null) ? Matcher.quoteReplacement(value) : "";
            matcher.appendReplacement(output, replacement);
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public static String extrairConteudoParenteses(@NonNull String txt) {
        var builder = new StringBuilder(txt);
        int start = builder.indexOf("(");
        while(start != -1) {
            if(builder.indexOf(")") == -1) break;
            builder.delete(0, start+1);
            start = builder.indexOf("(");
        }
        int end = builder.indexOf(")");
        while(end != -1) {
            builder.delete(end, builder.length());
            end = builder.indexOf(")");
        }
        return builder.toString()
            .trim()
            .replaceAll("\\(", "");
    }

    public static String abstrairVariavel(@NonNull String texto, @NonNull String regex) {
        return texto.replaceAll(regex, "*");
    }

    public static List<String> javascriptString(List<String> texto) {
        if(texto == null) return List.of();
        return texto.stream().map(FormatString::javascriptString).toList();
    }

    public static String javascriptString(String texto) {
        if(texto == null) return "``";
        return "`" + refinarTextoJavascript(texto) +  "`";
    }

    public static int contarSubstring(@NotBlank String texto, @NotBlank String substring) {
        int contador = 0;
        int indice = texto.indexOf(substring);
        while (indice != -1) {
            contador++;
            indice = texto.indexOf(substring, indice + 1);
        }
        return contador;
    }

    public static <T> String tabelaParaString(@NotNull List<@NotNull List<T>> tabela) {
        if(tabela.isEmpty() || tabela.get(0).isEmpty()) return "";

        //Encontra o tamanho máximo de cada coluna
        int numColunas = tabela.get(0).size();
        int[] tamanhosMaximos = new int[numColunas];
        for(var linha : tabela) {
            for(int col = 0; col < linha.size(); col++) {
                int tamanhoValor = String.valueOf(linha.get(col)).length();
                if(tamanhoValor > tamanhosMaximos[col]) {
                    tamanhosMaximos[col] = tamanhoValor;
                }
            }
        }
        //Ajusta o tamanho de cada valor
        val linhaAlinhada = new StringBuilder();
        for(int linha = 0; linha < tabela.size(); linha++) {
            if(linha == 1) {
                val tamanhoLinha = Arrays.stream(tamanhosMaximos)
                    .sum()
                    + (numColunas * SEPARADOR_TABELA_COLUNA.length());
                linhaAlinhada.append(
                    preencherNaEsquerda("\n", tamanhoLinha, SEPARADOR_TABELA_HEADER)
                );
            }
            for(int col = 0; col < tabela.get(linha).size(); col++) {
                val colunasMiolo = col < (tabela.get(linha).size() -1);
                String valor = String.valueOf(tabela.get(linha).get(col));
                linhaAlinhada.append(
                    String.format("%-" + tamanhosMaximos[col] + "s", valor)
                        .concat(colunasMiolo ? SEPARADOR_TABELA_COLUNA : "")
                );
            }
            linhaAlinhada.append("\n");
        }
        return linhaAlinhada.toString();
    }

    public static String adicionarZerosNaEsquerda(@NonNull String texto, int tamanhoTexto) {
        return preencherNaEsquerda(texto, tamanhoTexto, '0');
    }

    public static String preencherNaEsquerda(
        @NonNull String texto, int tamanhoTexto, char caractere) {
        //--------------------------------------------------------
        val builder = new StringBuilder();
        while(builder.length() + texto.length() < tamanhoTexto) {
            builder.append(caractere);
        }
        return builder + texto;
    }

    public static String adicionarZerosNaDireita(@NonNull String texto, int tamanhoTexto) {
        return preencherNaDireita(texto, tamanhoTexto, '0');
    }

    public static String preencherNaDireita(
        @NonNull String texto, int tamanhoTexto, char caractere) {
        //--------------------------------------------------------
        val builder = new StringBuilder();
        while(builder.length() + texto.length() < tamanhoTexto) {
            builder.append(caractere);
        }
        return texto + builder;
    }


    /**
     * Método destinado a tratar textos que possam representar mais de um valor (uma lista). Esse tratmento
     * foi originalmente criado para tratar conteúdo obtido de células Excel. </br>
     * Os caracteres de separação são: quebra-de-linha ("\n") e ponto-e-vírgula (";").
     * @param valor {@link String} a ser tratado
     * @return {@link List} {@link String} podendo ter 0 ou mais textos
     */
    public static List<String> dividirValores(String valor) {
        if(valor == null || valor.isEmpty()) return new ArrayList<>();

        //Adicionando um indicador no final para garantir que textos sem indicadores fiquem OK
        val valorRefinado = refinarCelula(valor)
            .replace("\n", ";")
            .replace(", ", ";")
            .concat(";");

        //Dividindo texto
        return Stream.of(valorRefinado.split(";"))
            .filter(texto -> !texto.isEmpty())
            .toList();
    }

    //TODO: Trocar String para algo mais performatico
    public static String extrairMascara(String mascara) {
        var textoRefinado = refinarCelula(mascara);
        for(var item : MascaraCoringa.values())
            textoRefinado = item.algoritmo.apply(textoRefinado);
        return textoRefinado;
    }

    public static String lastSubstring(@NotBlank String text, @NotNull int quantidade) {
        if(text.length() >= quantidade) { return text.substring(0, quantidade-1); }
        return text.substring(0, 0);
    }

    public static String lastSubstring(@NotBlank String text, @NotBlank String charSequence) {
        final String[] stringArray = text.split(charSequence);
        final int stringSize = stringArray.length-1;
        return stringSize >= 0 ? stringArray[stringSize] : text;
    }

    public static String getLastMatch(@NotBlank String text, @NotBlank String targetCharSequence) {
        text = text.toLowerCase();
        int index = text.indexOf(targetCharSequence);
        if(index == -1) { return text; }
        return text.substring(index+1).trim();
    }

    public static String getLastMatch(@NotNull List<String> text, @NotBlank String targetCharSequence) {
        String textoFinal = "";
        for(String txt : text) {
            textoFinal += getLastMatch(txt, targetCharSequence) + " ";
        }
        return textoFinal;
    }

    public static String stackTraceToString(@NotNull StackTraceElement[] stack) {
        String finalStack = "";
        //Tratando cada Element da StrackTraceElement
        for(StackTraceElement element : stack) {

            //Convertendo Element para String
            final String frase = element.toString();

            //Dividindo a frase em palavras, tendo como divisória o sinal "."
            String[] palavras = frase.split("\\.");

            //Recortando apenas o que importa: 3 últimas palavras da frase
            String text = Stream.of(palavras)
            .skip(palavras.length - 2)
            .collect(Collectors.joining("."));
            finalStack += text + "\n";
        }
        return finalStack;
    }

    public static Map<String, String> mapStackTrace(@NotNull StackTraceElement[] stack) {
        final Map<String, String> mappedStack = new HashMap<>();
        short stackSize = (short) stack.length;

        //Tratando cada Element da StrackTraceElement
        for(StackTraceElement element : stack) {
            //Convertendo Element em palavras separados por "."
            final String frase = element.toString();
            final String index = String.format("%03d", stackSize--);
            String[] palavras = frase.split("\\.");

            //Recortando apenas o que importa: 3 últimas palavras da frase
            String text = Stream.of(palavras)
                .skip(palavras.length - 2)
                .collect(Collectors.joining("."));
            //mappedStack += text + "\n";
            mappedStack.put(index, text);
        }
        return mappedStack;
    }

    public static List<String> arrayStackTrace(@NotNull StackTraceElement[] stack) {
        short stackSize = (short) stack.length;
        final List<String> listStack = new ArrayList<>();

        //Tratando cada Element da StrackTraceElement
        for(StackTraceElement element : stack) {

            //Convertendo Element em palavras separados por "."
            final String frase = element.toString();
            String[] palavras = frase.split("\\.");

            //Recortando apenas o que importa: 3 últimas palavras da frase
            String text = Stream.of(palavras)
                .skip(palavras.length - 2)
                .collect(Collectors.joining("."));
            //listStack += text + "\n";
            listStack.add(text);
        }
        return listStack;
    }

    public static String getMainException(@NotBlank String text) {
        return getBetween(text, "(", ")");
    }

    //TODO: Aperfeiçoar com Java 8
    public static String getBetween(
        @NotBlank String text, @NotBlank String charSequenceStart, @NotBlank String charSequenceEnd) {
        //--------------------------------------------------------------------------------------------
        final int start = text.indexOf(charSequenceStart);
        final int end = text.indexOf(charSequenceEnd);
        return text.substring(start, end);
    }

}
