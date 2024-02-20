package br.com.ppw.dma.exception;

public class OperacaoSftpException extends RuntimeException {

    public OperacaoSftpException(String comando, String mensagemErro) {
        super(mensagem(comando, mensagemErro));
    }

    public static String mensagem(String comando, String mensagemErro) {
        return String.format("Erro ao tentar executar o comando '%s': %s.", comando, mensagemErro);
    }

}
