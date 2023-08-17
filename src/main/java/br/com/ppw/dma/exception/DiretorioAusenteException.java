package br.com.ppw.dma.exception;

public class DiretorioAusenteException extends CheckedException {
    private static final String MENSAGEM = "Diretório '%s' não encontrado";

    public DiretorioAusenteException(String diretorio) {
        super(getMensagem(diretorio));
    }

    public static String getMensagem(String arquivoNome) {
        return String.format(MENSAGEM, arquivoNome);
    }

}
