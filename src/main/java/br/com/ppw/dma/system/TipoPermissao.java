package br.com.ppw.dma.system;

public enum TipoPermissao {

    ESCREVER("escrever"),
    LER("ler"),
    EXECUTAR("executar");

    public final String operacao;

    TipoPermissao(String operacao) {
        this.operacao = operacao;
    }
}
