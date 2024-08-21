package br.com.ppw.dma.config;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.stream.Collectors;

import static ch.qos.logback.core.CoreConstants.LINE_SEPARATOR;

public class CustomLog extends PatternLayoutEncoder {

    private static int contador = 0;
    private static final int CONTADOR_MAX = 10;
    static String ultimoTopico = "õ%è$%¨#$1⌂!&&@";

    @Override
    public byte[] encode(ILoggingEvent event) {
        String formattedLog = createFormattedLog(event);
        return formattedLog.getBytes();
    }

    private String createFormattedLog(ILoggingEvent event) {
        var logBuilder = new StringBuilder(128);
        var infos = getLayout().doLayout(event).split(LINE_SEPARATOR);

        if(!infos[0].equals(ultimoTopico)) {
            ultimoTopico = infos[0];
            logBuilder.append(LINE_SEPARATOR)
                .append(infos[0])
                .append(LINE_SEPARATOR);
        }
//        contador += formattedLogs.length;

        return logBuilder + event.getFormattedMessage()
            .lines()
            .filter(linha -> !linha.isBlank())
//                .peek(linha -> contador++)
            .map(linha -> "(" + event.getLevel().levelStr + ") " + infos[1] + ": " + linha)
            .collect(Collectors.joining(LINE_SEPARATOR))
            + LINE_SEPARATOR
//            + event.get
        ;
    }
}
