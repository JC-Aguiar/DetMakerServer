package br.com.ppw.dma.exception;

public class ClienteAusenteException extends RuntimeException {
    private static final String MENSAGEM = "ID do Cliente não informado.";

    public ClienteAusenteException() {
        super(MENSAGEM);
    }

}
