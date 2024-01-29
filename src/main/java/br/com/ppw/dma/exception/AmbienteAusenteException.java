package br.com.ppw.dma.exception;

public class AmbienteAusenteException extends RuntimeException {
    private static final String MENSAGEM = "ID do Ambiente não informado.";

    public AmbienteAusenteException() {
        super(MENSAGEM);
    }

}
