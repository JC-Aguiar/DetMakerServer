package br.com.ppw.dma.config;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

import static br.com.ppw.dma.config.CustomLog2.ultimoTopico;
import static ch.qos.logback.core.CoreConstants.LINE_SEPARATOR;

public class CustomLog3 extends PatternLayoutEncoder {

    @Override
    public byte[] encode(ILoggingEvent event) {
        String formattedLog = createFormattedLog(event);
        return formattedLog.getBytes();
    }

    private String createFormattedLog(ILoggingEvent event) {
        var logBuilder = new StringBuilder(128);
        var infos = getLayout().doLayout(event)
            .split(LINE_SEPARATOR);

        if(!infos[0].equals(ultimoTopico)) {
            ultimoTopico = infos[0];
            logBuilder.append(LINE_SEPARATOR)
                .append(infos[0])
                .append(LINE_SEPARATOR);
        }
        Arrays.stream(infos)
            .skip(1)
            .peek(logBuilder::append)
            .forEach(linha -> logBuilder.append(LINE_SEPARATOR));
//        contador += formattedLogs.length;

        return logBuilder.toString();
    }
}
