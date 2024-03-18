package br.com.ppw.dma.net;

public enum RemoteFileComparetorStatus {

    NOT_DONE("A aplicação não teve oportunidade de tentar coletar os arquivos.", true),
    DUPLICATED("Os arquivos coletados são os mesmos", true),
    NOT_FOUND("O arquivo não foi encontrado pós-execução.", true),
    SUCCESS("Arquivos coletados com sucesso.", false),
    INVALID("A referência do diretório/arquivo está inválida.", true);

    public final String message;
    public final boolean error;

    RemoteFileComparetorStatus(String message, boolean error) {
        this.message = message;
        this.error = error;
    }
}
