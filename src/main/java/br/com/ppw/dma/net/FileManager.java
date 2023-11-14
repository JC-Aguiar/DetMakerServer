package br.com.ppw.dma.net;

import br.com.ppw.dma.system.Arquivos;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Data
@Slf4j
//TODO: Javadoc
public class FileManager {
    final String reference;
    final Path path; //TODO: necessário mesmo?
    final List<File> files = new ArrayList<>();

    public FileManager addFile(@NotBlank String reference, @NonNull File file) {
        if(!this.reference.equals(reference)) {
            log.trace("Incompatibilidade no gerenciamento de arquivos: a referência não é a mesma.");
            log.trace(this.toString());
            log.trace(file.toString());
            return this;
        }
        this.files.add(file);
        return this;
    }

    public FileManager addFile(@NonNull FileManager file) {
        if(!file.reference.equals(reference)) {
            log.trace("Incompatibilidade no gerenciamento de arquivos: a referência não é a mesma.");
            log.trace(this.toString());
            log.trace(file.toString());
            return this;
        }
        this.files.addAll(file.getFiles());
        return this;
    }

    public Optional<File> latestModified() {
        printFiles();
        if(this.files.size() < 2) {
            log.warn("Não existem 2 logs (antes e depois) disponíveis para se comparar.");
            return Optional.empty();
        }
        //Ordenando arquivos elos mais recentes primeiro
        val mostRecentOnes = this.files.stream()
            .sorted(Comparator.comparing(File::lastModified).reversed())
            .limit(2)
            .toList();
        if(mostRecentOnes.get(0).lastModified() == mostRecentOnes.get(1).lastModified()) {
            log.warn("Ambos os 2 logs (antes e depois) possuem a mesma data de modificação.");
            log.warn("Isso indica que algo de errado ocorreu. Nenhum arquivo de log será disponibilziado.");
            return Optional.empty();
        }
        return Optional.of(mostRecentOnes.get(0));
        //.orElseThrow(() -> new NoSuchFieldException("Nenhum arquivo disponível."));
        //long lastModified1 = files == null ? Long.MAX_VALUE : files.lastModified();
        //long lastModified2 = actual == null ? Long.MAX_VALUE : actual.lastModified();
        //return (lastModified1 > lastModified2) ? files : actual;
    }

    public void printFiles() {
        log.info("Referência: '{}'", reference);
        log.info("\t > Diretório: {} ", path);
        this.files.forEach(Arquivos::printInfo);
    }
}
