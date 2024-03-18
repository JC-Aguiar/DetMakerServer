package br.com.ppw.dma.exception;

public class OperacaoSftpException extends RuntimeException {

    public final String comando;

    public OperacaoSftpException(String comando, String mensagemErro) {
        super(mensagemErro);
        this.comando = comando;
    }

//    public static String mensagem(String comando, String mensagemErro) {
//        return String.format("Erro ao executar o comando '%s': %s.", comando, mensagemErro);
//    }

}
