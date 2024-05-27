package br.com.ppw.dma.config;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;

import java.util.Arrays;

import static br.com.ppw.dma.util.FormatDate.FORMAL_STYLE;

public class CustomLog extends LayoutBase<ILoggingEvent> {

    private static int contador = 0;
    private static String ultimoTopico = "";
    static PatternLayout layoutTopico;
    static PatternLayout layoutAop;
    static PatternLayout layoutExcception;
    private static final int CONTADOR_MAX = 10;;

    {
        layoutTopico = new PatternLayout();
//        layoutTopico.setPattern("%d{yyyy/MM/dd HH:mm:ss} | %thread | %logger{0}.%method%n");
        //SPRING: %clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%5p) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n%wEx
        layoutTopico.setPattern("%n%date{yyyy/MM/dd HH:mm:ss} -- %thread -- %class{0}.%method%n");
        layoutTopico.setContext(getContext());
        layoutTopico.start();

        layoutAop = new PatternLayout();
        layoutAop.setPattern("%n%date{yyyy/MM/dd HH:mm:ss} | %15thread | %logger%n");
        layoutAop.setContext(getContext());
        layoutAop.start();

        layoutExcception = new PatternLayout();
//        layoutLog.setPattern("%level: %replace(%msg){'%n', ' '}");
//        layoutLog.setPattern("  %5.5level: %msg%n%xEx");
        layoutExcception.setPattern("%xEx");
        layoutExcception.setContext(getContext());
        layoutExcception.start();
    }

    public String doLayout(ILoggingEvent event) {
        final StringBuffer logBuilder = new StringBuffer(128);
//        final String pid = Optional.ofNullable(MDC.get("PID")).orElse(" ");
//        final LogStep step = Arrays.stream(LogStep.values())
//            .filter(ls -> mensagem.startsWith(ls.call))
//            .findFirst()
//            .orElse(null);

//        if(layoutTopico == null) initializeLayoutEncoder();

        var topico = layoutTopico.doLayout(event)
            .replace("ConsoleLogAspect.identifyBefore", event.getLoggerName())
            .replace("ConsoleLogAspect.identifyAfter", event.getLoggerName());
//        if(event.getLoggerName().equals(ConsoleLogAspect.class.getName())) {
//            logBuilder.append(layoutAop.doLayout(event));
//        }
//        else if(contador > CONTADOR_MAX || (!ultimoTopico.equals(topico) && !topico.isBlank())) {
        if(contador > CONTADOR_MAX || !ultimoTopico.equals(topico)) {
            contador = 0;
            ultimoTopico = topico;
            logBuilder.append(ultimoTopico);
        }

//        Arrays.stream(event.getFormattedMessage().split(CoreConstants.LINE_SEPARATOR))
//            .filter(linha -> !linha.isBlank())
//            .peek(linha -> contador++)
//            .forEach(layoutLog::doLayout);
//            .peek(linha -> logBuilder.append(event.getLevel().levelStr + ": "))
//            .forEach(logBuilder::append);
        Arrays.stream(event.getFormattedMessage().split("\\R"))
//            .replaceAll("\\R", " ")
//            .lines()
            .filter(linha -> !linha.isBlank())
            .peek(linha -> contador++)
            .forEach(linha -> logBuilder
                .append(event.getLevel().levelStr)
                .append(": ")
                .append(linha)
                .append(CoreConstants.LINE_SEPARATOR));
        logBuilder.append(layoutExcception.doLayout(event));
        contador += event.getCallerData().length;
//        contador++;
        return logBuilder.toString();
    }

    public static String printObjeto(Object obj) {
        obj += ".";
        return obj.toString().replaceFirst("\\(", ": ").replace(").", "");
    }
}