package br.com.ppw.dma.util;

import br.com.ppw.dma.domain.master.SqlSintaxe;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.ppw.dma.domain.master.SqlSintaxe.DqlKeywords.SELECT;

@Slf4j
public abstract class BashSintaxe {

    public static Set<String> findQueriesInFile(@NonNull File file)
        throws IOException {

        /*
        TODO:
            VALIDAÇÃO
            Sintaxe:
                Antes de qualquer etapa, o sistema deve validar a sintaxe do script usando o comando:
                bash -n <nome do arquivo>.sh
            IDENTIFICAR CONTEXTOS
            Contexto Privado:
                Identificar quais blocos de código são do contexto raiz e quais são de métodos.
                Lendo linha a linha, procurar por "() {", ignorando espaços em branco entre esses caracteres.
                O primeiro nome antes desse padrão deverá ser capturado (nome da função no script).
                Possíveis regex:
                    ^(?!.*\#).*?(\w+)\s*\(\)\s*(?:\r?\n\s*)*\{
                    ^(?!.*#).*?(\w+)\s*\(\)\s*(?:\r?\n\s*)*\{
                Agora será necessário iterar cada linha e contar os caracteres "{" e "}". O contador inicia em 1
                e para cada caractere "{" identificado na linha, é incrementado. O oposto também acontece para o
                o caractere "}", decrementando o contador.
                Quando o contador chegar em 0, temos o aí o escopo da função mapeada.
                O contador deve ignorar se qualquer um desses caracteres feio depois de um "#".
            Contexto Raiz:
                Possuindo o Contexto Privado em mãos, iremos remover estes do resto, obtendo assim os códigos
                que sao do Contexto Raiz.
            IDENTIFICAR PROPRIEDADES
            Parâmetros Obrigatorios:
                Analisando os códigos do Contexto Raiz, procurar por "$#" ou por "$1", "$2", "$3"... "$n".
                Essas declarações não podem estar após um comando "set" ou "shift".
                Se existem variáveis sendo atribuídas a esses comandos, gravar associação.
                Se identificado "$#", validar se ele está em 1 "if"
         */

        log.info("Procurando por queries no arquivo '{}'", file.getName());
        if(!file.exists()) throw new IOException("Arquivo não encontrado.");
        if(!file.canRead()) throw new IOException("Arquivo sem permissão de leitura.");

        var keywords = Stream.of(SqlSintaxe.DdlKeywords.values())
            .map(SqlSintaxe.DdlKeywords::name)
            .collect(Collectors.toSet());
        keywords.addAll(SqlSintaxe.INVOKE_KEYWORDS);
        keywords.addAll(SELECT.keywords);

        Function<String, Set<String>> textoToQueries = (texto) -> {
            return Stream.of(texto.split(";"))
                .map(String::trim)
                .map(SqlSintaxe::formatQuery)
                .filter(txt -> keywords.stream().anyMatch(txt::startsWith))
                .filter(txt -> !txt.contains("COLUMN"))
                .filter(txt -> !txt.contains("INDEX"))
                .collect(Collectors.toSet());
        };

        try(var reader = new BufferedReader(new FileReader(file))) {
            var content = reader.lines().toList();

            log.info("Obtendo queries em textos literais.");
            var quotePattern = Pattern.compile("\"([^\"]*)\"");
            var queries = content.stream()
                .map(quotePattern::matcher)
                .map(matcher -> {
                    // Adiciona o conteúdo encontrado
                    var resultado = new StringBuilder();
                    while(matcher.find()) resultado.append(matcher.group(1)).append(" ");
                    return resultado.toString();
                })
                .filter(txt -> !txt.isBlank())
                .map(txt -> {
                    if(!txt.contains("--")) return txt;
                    var index = txt.indexOf("--");
                    return txt.substring(0, index);
                })
                .map(textoToQueries)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

            log.info("Obtendo queries em blocos SQL*Plus.");
            var queryPattern = Pattern.compile(
                "sqlplus.*?<<\\s*(\\w+)\\s*([\\s\\S]*?)\\n\\1\\s*$",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            var matcher = queryPattern.matcher(String.join("\n", content));
            var resultado = new StringBuilder();
            while(matcher.find()) {
                var queryBlock = Stream.of(matcher.group(2).split("\n"))
                    .map(String::toUpperCase)
                    .map(String::trim)
                    .filter(txt -> !txt.startsWith(">>"))
                    .filter(txt -> !txt.startsWith("$"))
                    .filter(txt -> !txt.startsWith("SET"))
                    .map(txt -> {
                        if(!txt.contains("--")) return txt;
                        var index = txt.indexOf("--");
                        return txt.substring(0, index);
                    })
                    .collect(Collectors.joining("\n"));
                resultado.append(queryBlock).append(";\n");
            }
            var sqlBlock = resultado.toString();
            queries.addAll(textoToQueries.apply(sqlBlock));

//            System.out.println("-------------------------------");
//            System.out.println("Blocos SQL*Plus:");
//            System.out.println(sqlBlock);

//            System.out.println("-------------------------------");


            log.info("Coletados:");
            queries.forEach(log::info);

//            queries.stream()
//                .peek(txt -> log.info("Comando: {}", txt))
//                .map(SqlSintaxe::getTablesNameFromQuery)
//                .flatMap(Collection::stream)
//                .forEach(tableNames::add);
//
//            var keywords = Arrays.stream(DdlKeywords.values())
//                .map(DdlKeywords::name)
//                .collect(Collectors.toSet());
//            keywords.addAll(INVOKE_KEYWORDS);
//            log.info("Keywords: {}", keywords);
//
//            queries.stream()
//                .peek(txt -> log.info("Comando: {}", txt))
//                .map(txt -> SqlSintaxe.freeExtractFromQuery(txt, keywords, DDL_CONTEXT_KEYWORDS))
//                .flatMap(Collection::stream)
//                .map(txt -> {
//                    var split = Arrays.asList(txt.split("\\."));
//                    if(split.size() == 1) return txt;
//                    return split.stream().skip(1).collect(Collectors.joining("."));
//                })
//                .peek(log::info)
////                .map(SqlSintaxe::getTablesNameFromQuery)
//                .forEach(tableNames::add);
//
//            log.info("Total de tabelas identificadas: [{}]", tableNames.size());
//            tableNames.forEach(log::info);
            return queries;
        }
        catch(IOException e) {
            throw e;
        }
    }

}
