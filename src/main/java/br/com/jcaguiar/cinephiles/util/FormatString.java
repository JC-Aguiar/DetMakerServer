package br.com.jcaguiar.cinephiles.util;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FormatString {

    public static String subString(@NotBlank String text, @NotNull int quantidade) {
        if (text.length() >= quantidade) { return text.substring(0, quantidade-1); }
        return text.substring(0, 0);
    }

    public static String subString(@NotBlank String text, @NotBlank String charSequence) {
        final String[] stringArray = text.split(charSequence);
        final int stringSize = stringArray.length-1;
        return stringSize >= 0 ? stringArray[stringSize] : text;
    }

    public static String getLastMatch(@NotBlank String text, @NotBlank String targetCharSequence) {
        text = text.toLowerCase();
        int index = text.indexOf(targetCharSequence);
        if (index == -1) { return text; }
        return text.substring(index+1).trim();
    }

    public static String getLastMatch(@NotNull List<String> text, @NotBlank String targetCharSequence) {
        String textoFinal = "";
        for (String txt : text) {
            textoFinal += getLastMatch(txt, targetCharSequence) + " ";
            }
        return textoFinal;
    }

    public static String stackTraceToString(@NotNull StackTraceElement[] stack) {
        String finalStack = "";
        //Tratando cada Element da StrackTraceElement
        for (StackTraceElement element : stack) {
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
        for (StackTraceElement element : stack) {
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
        for (StackTraceElement element : stack) {
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
    public static String getBetween(@NotBlank String text, @NotBlank String charSequenceStart, @NotBlank String charSequenceEnd) {
        final int start = text.indexOf(charSequenceStart);
        final int end = text.indexOf(charSequenceEnd);
        return text.substring(start, end);
    }

    public static int stringToInt(String text) throws NumberFormatException {
        text = text.replace(',', '.');
        int valor = Integer.parseInt(text);
        if (valor < 0) { throw new NumberFormatException("Valor negativo."); }
        return valor;
    }

    public static double stringToDouble(String text) throws NumberFormatException {
        text = text.replace(',', '.');
        double valor = Double.parseDouble(text);
        if (valor < 0) { throw new NumberFormatException("Valor negativo."); }
        return valor;
    }

    public static BigDecimal stringToBigInt(String text) throws NumberFormatException {
        text = text.replace(',', '.');
        double valor = Double.parseDouble(text);
        if (valor < 0) { throw new NumberFormatException("Valor negativo."); }
        return BigDecimal.valueOf(valor);
    }

}
