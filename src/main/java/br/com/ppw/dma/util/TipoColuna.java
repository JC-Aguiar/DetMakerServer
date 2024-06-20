package br.com.ppw.dma.util;

import br.com.ppw.dma.configQuery.ConfigQueryVar;
import br.com.ppware.GeradorAleatorio;
import br.com.ppware.NumeroAleatorio;
import br.com.ppware.TempoAleatorio;
import lombok.NonNull;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public enum TipoColuna {
    //Tipos mais comuns em banco:
    INT(TipoColuna::randomInteger, "%s" ),
    LOB(TipoColuna::randomString, "'%s'"),
    CHAR(TipoColuna::randomString, "'%s'"),
    LONG(TipoColuna::randomString, "'%s'"),
    DATE(TipoColuna::randomOffsetDateTime, "TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')"),
    REAL(TipoColuna::randomFloat, "%s"),
    FLOAT(TipoColuna::randomFloat, "%s"),
    DOUBLE(TipoColuna::randomFloat, "%s"),
    NUMBER(TipoColuna::randomBigDecimal, "%s"),
    DECIMAL(TipoColuna::randomBigDecimal, "%s"),
    NUMERIC(TipoColuna::randomBigDecimal, "%s"),
    TIMESTAMP(TipoColuna::randomOffsetDateTime, "TO_TIMESTAMP('%s', 'DD/MM/YYYY HH24:MI:SS.FF3')"),
    //Tipo para controle interno:
    UNSET(null, "") //TODO: exceção própria
    ;

    private final Function<ConfigQueryVar, String> geradorAleatorio;
    private final String formatacao;
    private final Function<String, String> formatoSql;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");


    TipoColuna(Function<ConfigQueryVar, String> geradorAleatorio, String formatacao) {
        this.geradorAleatorio = geradorAleatorio;
        this.formatacao = formatacao;
        this.formatoSql = (input) -> String.format(formatacao, input);
    }

    public String gerarValorAleatorioSql(@NonNull ConfigQueryVar configQueryVar) {
        var valor = geradorAleatorio.apply(configQueryVar);
        return formatoSql.apply(valor);
    }

    
    private static String randomBigDecimal(@NonNull ConfigQueryVar queryVar) {
        return DECIMAL_FORMAT.format(
            NumeroAleatorio.novoBigDecimal(
                queryVar.getPrecisao() - queryVar.getEscala(),
                queryVar.getEscala()
        ));
    }
    
    private static String randomInteger(@NonNull ConfigQueryVar queryVar) {
        return NumeroAleatorio.XDigitosEmString(5);
    }

    private static String randomFloat(@NonNull ConfigQueryVar queryVar) {
        return NumeroAleatorio.novoValorMonetarioReal(10);
    }
    
    private static String randomString(@NonNull ConfigQueryVar queryVar) {
        var nome  = GeradorAleatorio.nome();
        if(nome.length() <= queryVar.getTamanho()) return nome;
        return nome.substring(0, queryVar.getTamanho());
    }
    
    private static String randomOffsetDateTime(@NonNull ConfigQueryVar queryVar) {
        return TempoAleatorio.dataHora().format(FormatDate.BRASIL_STYLE);
    }
    
    //    public static byte[] randomByteArray(@NonNull ConfigQueryVar queryVar) {
//        return NumeroAleatorio.XDigitosEmString(queryVar.getTamanho()).getBytes();
//    }
    
    public static List<TipoColuna> COLUNAS_TEXTUAIS = List.of(CHAR, LOB, LONG);
    public static List<TipoColuna> COLUNAS_NUMERICAS = List.of(
        INT, REAL, FLOAT, DOUBLE, NUMBER, DECIMAL, NUMERIC);
    public static List<TipoColuna> COLUNAS_DATAVEIS = List.of(DATE, TIMESTAMP);

    //TODO: javadoc
    //TODO: throw próprio?
    public static TipoColuna from(@NonNull String texto) {
        for(var lista : List.of(COLUNAS_TEXTUAIS, COLUNAS_NUMERICAS, COLUNAS_DATAVEIS)) {
            var tipo = lista.stream()
                .sorted(Comparator.comparing(Enum::ordinal))
                .filter(opcao -> texto.toUpperCase().contains(opcao.name()))
                .findFirst();
            if(tipo.isPresent()) return tipo.get();
        }
        throw new RuntimeException(mensagemErro(texto));
    }
    
    private static String mensagemErro(@NonNull String tipoInvalido) {
        return "Nenhuma tratamento mapeado para coluna do tipo '" +tipoInvalido+ "'.";
    }
}
