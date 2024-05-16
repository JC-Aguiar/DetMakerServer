package br.com.ppw.dma.exception;

public class SummaryErrorWithNoMessage extends Throwable {

    public SummaryErrorWithNoMessage() {
        super(
            "O método de processamento retornou falha, mas sem descrição." +
            "Contate a equipe técnica para analisar o caso."
        );
    }
}
