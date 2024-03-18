package br.com.ppw.dma.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.WARN;

public class CustomLog extends LayoutBase<ILoggingEvent> {

    private static final int NOME_MAX = 24;
    private static final int TAB = 3;
    private static final DateTimeFormatter DATA_FORMATO = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static LocalDateTime ultimaData = LocalDateTime.now()
        .minusYears(1L)
        .with(ChronoField.MILLI_OF_SECOND, 0)
        .withNano(0);
    private static String ultimaThread = "";
    private static String ultimoLogger = "";

    public String doLayout(ILoggingEvent event) {
        final StringBuffer logBuilder = new StringBuffer(128);
//        final String pid = Optional.ofNullable(MDC.get("PID")).orElse(" ");
        final String mensagem = formatarLog(event.getFormattedMessage());
        final LocalDateTime agora = LocalDateTime.now()
            .with(ChronoField.MILLI_OF_SECOND, 0)
            .withNano(0);
        final LogStep step = Arrays.stream(LogStep.values())
            .filter(ls -> mensagem.startsWith(ls.call))
            .findFirst()
            .orElse(null);

        if(ultimaData.isBefore(agora) || !ultimaThread.equals(event.getThreadName())) {
            ultimaData = agora;
            ultimaThread = event.getThreadName();
            ultimoLogger = "";
            logBuilder
                .append(CoreConstants.LINE_SEPARATOR)
                .append(agora.format(DATA_FORMATO))
                .append(" | THREAD ")
                .append(ultimaThread)
                .append(CoreConstants.LINE_SEPARATOR);
        }
        if(!ultimoLogger.equals(event.getLoggerName())) {
            ultimoLogger = event.getLoggerName();
            logBuilder
                .append(tab())
                .append("LOGGER ")
                .append(ultimoLogger)
                .append(CoreConstants.LINE_SEPARATOR);
        }
        logBuilder.append(tab());
        if(step == null) {
            logBuilder
                .append(formatarLevel(event.getLevel()))
                .append(mensagem);
        }
        else {
            logBuilder.append(mensagem.replace(step.call, step.log).trim());
        }
        return logBuilder
            .append(CoreConstants.LINE_SEPARATOR)
            .toString();
    }

    public static String formatarLog(String mensagem) {
        return mensagem
            .replace(CoreConstants.LINE_SEPARATOR, " ")
            .replaceAll("\\s+", " ");
    }

    public static String formatarHibernateQuery(String query) {
        return formatarLog(
            query.replace("Hibernate:", tab() + LogStep.SQL.log));
    }

    public static String tab() {
        return " ".repeat(TAB);
    }

    private String formatarThread(String thread) {
        if(thread.length() <= NOME_MAX) return formatarThreadEspaco(thread);
        thread = thread.replace(" ", "$")
            .replace("\\", "$")
            .replace("|", "$")
            .replace("/", "$");
        final List<String> threadList = Arrays.stream(thread.split("\\$"))
            .filter(l -> !l.isEmpty())
            .collect(Collectors.toList());
        thread = threadList.get(threadList.size()-1);
        thread = thread.length() > NOME_MAX ? thread.substring(0, NOME_MAX) : thread;
        return formatarThreadEspaco(thread);
    }

    private String formatarThreadEspaco(String thread) {
        final StringBuilder builder = new StringBuilder(thread);
        while (builder.length() < NOME_MAX) {
            builder.append("-");
        }
        return builder.toString();
    }

    private String formatarLevel(Level level) {
        if(level == INFO || level == WARN)
            return level.levelStr + "    ";
        return level.levelStr + "   ";
    }

    private String formatarNome(String nome) {
        if(nome.length() <= NOME_MAX) return nome;
        final StringBuilder builder = new StringBuilder();
        builder.append(nomeSimples(nome, "."));
        while (builder.length() < NOME_MAX) {
            builder.append(".");
        }
        return builder.toString();
    }

    private String nomeSimples(String nome, String removerChar) {
        if(!nome.contains(removerChar)) return nome;
        final String[] nomePath = nome.split(removerChar);
        return nomePath[nomePath.length-2] + removerChar + nomePath[nomePath.length-1];
    }

    public static String printObjeto(Object obj) {
        obj += ".";
        return obj.toString().replaceFirst("\\(", ": ").replace(").", "");
    }
}