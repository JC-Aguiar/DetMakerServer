package br.com.ppw.dma.net;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static br.com.ppw.dma.net.RemoteFileComparetorStatus.*;
import static br.com.ppw.dma.net.RemoteFileComparetorStatus.SUCCESS;

@Slf4j
@Getter
@ToString
@EqualsAndHashCode
//TODO: Javadoc
public final class SftpFileManager<T> {

    private final String comando;
//    private final SftpCommandType type;
    private final Optional<T> file;
    private boolean success = false;
    private String erro = "";

    public static final String NOT_FOUND = "Arquivo não encontrado.";
    public static final String SUCCESS = "Arquivos coletado com sucesso.";


//    public SftpFileManager(@NonNull SftpCommandType type, String reference, T obj) {
    public SftpFileManager(String comando, T obj) {
        this.comando = comando;
//        this.type = type;
        if(comando == null || comando.isEmpty()) {
            this.file = Optional.empty();
            this.erro = "Comando nulo/vazio é inválido.";
            return;
        }
        this.file = Optional.ofNullable(obj);
    }

    public void setSuccess(boolean success) {
        if(comando == null || comando.isEmpty())
            return;
        this.success = success;
    }

    public void setErro(String erro) {
        if(comando == null || comando.isEmpty())
            return;
        log.warn( "Erro inesperado: {}", erro);
        this.erro = erro;
        this.success = false;
    }

    //TODO: javadoc!!!!
    public static <T> SftpFileManager<T> compare(
        @NonNull SftpFileManager<T> antes,
        @NonNull SftpFileManager<T> depois) {
        //--------------------------------------------
        val arquivoAntes = antes.file;
        val arquivoDepois = depois.file;
        boolean duplicidade = arquivoAntes.isPresent()
            && arquivoDepois.isPresent()
            && arquivoAntes.equals(arquivoDepois);

        if(duplicidade) {
            val cenarioErro = new SftpFileManager<T>(depois.comando, null);
            cenarioErro.setErro(NOT_FOUND);
            return cenarioErro;
        }
        return depois;
    }

}
