package br.com.ppw.dma.exception;

public class FtpHostException extends RuntimeException {

    private static final String MENSAGEM = "O endereço de acesso não está no formato válido. " +
        "Precisa no padrão ${ip}:${porta}";

    public FtpHostException() {
        super(MENSAGEM);
    }

}
