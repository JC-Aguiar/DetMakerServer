package br.com.ppw.dma.net;

public enum UploadManagerStatus {

    NOT_DONE("A aplicação não teve oportunidade de tentar enviar os arquivos.", true),
    NOT_FOUND("O diretório informado não foi encontrado.", true),
    SUCCESS("Arquivos enviados com sucesso.", false),
    INVALID("A referência do diretório/arquivo está inválida.", true);

    public final String message;
    public final boolean error;

    UploadManagerStatus(String message, boolean error) {
        this.message = message;
        this.error = error;
    }
}
