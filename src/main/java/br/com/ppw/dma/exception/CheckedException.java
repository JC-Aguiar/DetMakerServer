package br.com.ppw.dma.exception;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CheckedException extends Exception {

    public CheckedException(String mensagem) { super(mensagem); }
    public CheckedException(String mensagem, Level level, Exception e) {
        super(mensagem + ": " + e.getMessage());
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final String nome = stackTrace[stackTrace.length-2].getClassName();
        final Logger log = LoggerFactory.getLogger(nome);
        switch (level.levelStr) {
            case "ERRO":
                log.error(mensagem + ": " + e.getMessage());
                e.printStackTrace();
                break;
            case "WARN":
                log.warn(mensagem + ": " + e.getMessage());
                e.printStackTrace();
                break;
            case "INFO":
                log.info(mensagem + ": " + e.getMessage());
                e.printStackTrace();
                break;
            case "TRACE":
                log.trace(mensagem + ": " + e.getMessage());
                e.printStackTrace();
                break;
            default:
        }
    }

    public static CheckedException erro(String mensagem, Exception e) {
        return new CheckedException(mensagem, Level.ERROR, e);
    }

    public static CheckedException warn(String mensagem, Exception e) {
        return new CheckedException(mensagem, Level.WARN, e);
    }

}
