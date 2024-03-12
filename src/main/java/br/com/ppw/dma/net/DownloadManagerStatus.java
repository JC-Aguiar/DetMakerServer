package br.com.ppw.dma.net;

public enum DownloadManagerStatus {

    NOT_DONE("A aplicação não teve oportunidade de tentar coletar os arquivos.", true),
    DUPLICATED("Os arquivos coletados são os mesmos", true),
    NOT_FOUND("O arquivo não foi encontrado após a execução.", true),
    SUCCESS("Arquivos coletados com sucesso.", false),
    INVALID("A referência do diretório/arquivo está inválida.", true);

    public final String message;
    public final boolean error;

    DownloadManagerStatus(String message, boolean error) {
        this.message = message;
        this.error = error;
    }
}
