package br.com.ppw.dma.exception;


import br.com.ppw.dma.system.TipoPermissao;

public class DiretorioSemPermissaoException extends CheckedException {
    private static final String MENSAGEM = "Diretório '%s' sem permissão de %s";

    public DiretorioSemPermissaoException(String arquivoNome, TipoPermissao tipoPermissao) {
        super(getMensagem(arquivoNome, tipoPermissao));
    }

    public static String getMensagem(String arquivoNome, TipoPermissao tipoPermissao) {
        return String.format(MENSAGEM, arquivoNome, tipoPermissao.operacao);
    }
}
