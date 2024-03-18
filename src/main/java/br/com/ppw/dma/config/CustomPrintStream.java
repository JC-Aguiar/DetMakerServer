package br.com.ppw.dma.config;

import ch.qos.logback.core.CoreConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintStream;

import static br.com.ppw.dma.config.CustomLog.formatarHibernateQuery;

@Slf4j
public class CustomPrintStream extends PrintStream {

    public CustomPrintStream(PrintStream originalPrintStream) {
        super(originalPrintStream);
    }

    @Override
    public void print(String s) {
        if(s != null && s.contains("Hibernate:"))
            log.info(s.replace("Hibernate:", LogStep.SQL.call));
//            super.print(formatarHibernateQuery(s));
        else
            super.print(s);
    }

    @Override
    public void println(String x) {
       print(x + CoreConstants.LINE_SEPARATOR);
    }
}
