package br.com.ppw.dma.config;

public enum LogStep {
    SQL("@Query@", "SQL     "),
    INICIOU("@Start@", "INICIOU "),
    PROCESSO("@Step@", "PROCESSO"),
    TERMINOU("@Close@", "TERMINOU");

    public final String call;
    public final String log;

    LogStep(String call, String log) {
        this.call = call;
        this.log = log;
    }
}
