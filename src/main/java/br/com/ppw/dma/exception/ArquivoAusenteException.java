package br.com.ppw.dma.exception;

public class ArquivoAusenteException extends CheckedException {
    private static final String MENSAGEM = "Arquivo '%s' não encontrado dentro do diretório '%s'";

    public ArquivoAusenteException(String arquivoNome, String diretorio) {
        super(getMensagem(arquivoNome, diretorio));
    }

    public static String getMensagem(String arquivoNome, String diretorio) {
        return String.format(MENSAGEM, arquivoNome, diretorio);
    }

}
