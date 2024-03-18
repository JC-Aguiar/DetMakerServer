package br.com.ppw.dma.net;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static br.com.ppw.dma.net.RemoteFileComparetorStatus.*;

@Slf4j
@Data
//TODO: Javadoc
public class RemoteFileComparator {

    private final String reference;
    private Optional<RemoteFile> preFile = Optional.empty();
    private Optional<RemoteFile> postFile = Optional.empty();

    //static final String NOT_DONE = "A aplicação não teve oportunidade de tentar coletar os arquivos.",
    //static final String DUPLICATED = "Os arquivos coletados são os mesmos",
    //static final String PRE_FILE_NOT_FOUND = "O arquivo não foi encontrado pré-execução.",
    //static final String POST_FILE_NOT_FOUND = "O arquivo não foi encontrado pós-execução.",
    //static final String SUCCESS = "Arquivos coletados com sucesso.",
    //static final String INVALID = "A referência do diretório/arquivo está inválida.";


    public RemoteFileComparator(@NonNull String reference) {
        this.reference = reference;
    }

    public RemoteFileComparetorStatus getStatus() {
        printFiles();
        if(postFile.isEmpty())
            return NOT_FOUND;

        if(preFile.isPresent()) {
            if(preFile.get().iguais(postFile.get()))
                return DUPLICATED;
            else
                return SUCCESS;
        }
        return SUCCESS;
    }

    public void printFiles() {
        log.info("\t - Referência: '{}'", reference);
        log.info("\t - Arquivo pré-execução: {}", preFile);
        log.info("\t - Arquivo pós-execução: {}", postFile);
    }
}
