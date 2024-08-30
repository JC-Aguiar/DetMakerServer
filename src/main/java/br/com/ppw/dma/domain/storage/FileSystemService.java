package br.com.ppw.dma.domain.storage;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.job.JobCarga;
import br.com.ppw.dma.exception.StorageException;
import br.com.ppw.dma.exception.StorageFileNotFoundException;
import br.com.ppw.dma.config.StorageProperties;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@Service
@ToString
public class FileSystemService implements StorageService {

    private final Path pathLocation;

    @Autowired
    public FileSystemService(StorageProperties properties) {
        this.pathLocation = Paths.get(
            System.getProperty("user.home"),
            properties.getLocation());
        if(!this.pathLocation.toFile().exists()) {
            if(!this.pathLocation.toFile().mkdir())
                throw new RuntimeException("Não foi possível criar diretório do DetMaker: " + pathLocation);
        }
        log.info("Diretório do DetMaker: '{}'.", this.pathLocation.toAbsolutePath());
    }

    @Override
    public File store(MultipartFile file) {
        try {
            if(file.isEmpty()) throw new StorageException("O arquivo enviado está vazio!");

            Path destinationFile = doFilePath(file.getOriginalFilename());
            validade(destinationFile);
            try(InputStream inputStream = file.getInputStream()) {
                return save(inputStream, destinationFile);
            }
        }
        catch(IOException e) {
            throw new StorageException("Erro ao tentar salvar arquivo!", e);
        }
    }

    public File store(JobCarga file) {
        try {
            if(file.getConteudo().isEmpty()) throw new StorageException("O arquivo enviado está vazio!");

            Path destinationFile = doFilePath(file.getNome());
            validade(destinationFile);
            val conteudoBytes = file.getConteudo().getBytes();
            try(InputStream inputStream = new ByteArrayInputStream(conteudoBytes)) {
                return save(inputStream, destinationFile);
            }
        }
        catch(IOException e) {
            throw new StorageException("Erro ao tentar salvar arquivo!", e);
        }
    }

    public File store(ExecFile file) {
        try {
            if(file.getArquivo().isEmpty())
                throw new StorageException("O conteúdo do ExecFile ID " +file.getId()+ " está vazio!");

            Path destinationFile = doFilePath(file.getArquivoNome());
            validade(destinationFile);
            val conteudoBytes = file.getArquivo().getBytes();
            try(InputStream inputStream = new ByteArrayInputStream(conteudoBytes)) {
                return save(inputStream, destinationFile);
            }
        }
        catch(IOException e) {
            throw new StorageException("Erro ao tentar salvar arquivo!", e);
        }
    }

    private Path doFilePath(String name) {
        return this.pathLocation
            .resolve(Paths.get(name))
            .normalize()
            .toAbsolutePath();
    }

    //Validação de segurança
    private void validade(Path destinationFile) {
        if (!destinationFile.getParent().equals(this.pathLocation.toAbsolutePath())) {
            throw new StorageException("Não é permitido salvar arquivos fora do diretório configurado!");
        }
    }

    private File save(InputStream inputStream, Path destinationFile) throws IOException {
        Files.copy(inputStream, destinationFile, REPLACE_EXISTING);
        return destinationFile.toFile();
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.pathLocation, 1)
                .filter(path -> !path.equals(this.pathLocation))
                .map(this.pathLocation::relativize);
        }
        catch(IOException e) {
            throw new StorageException("Erro ao tentar ler arquivo!", e);
        }
    }

    @Override
    public Path load(String filename) {
        return pathLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) return resource;
            else throw new StorageFileNotFoundException("Não foi possível ler arquivo: " + filename);
        }
        catch(MalformedURLException e) {
            throw new StorageFileNotFoundException("Não foi possível ler arquivo: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            val totalArquivos = Files.walk(pathLocation)
                .filter(Files::isRegularFile)
                .count();
            log.info("Total de arquivos temporários a deletar: {}.", totalArquivos);
        }
        catch(IOException e) {
            log.error("Não foi possível acessar/ler os conteúdos no diretório temporário: {}", e.getMessage());
        }
        FileSystemUtils.deleteRecursively(pathLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(pathLocation);
        }
        catch (IOException e) {
            throw new StorageException("Não foi possível inicializar diretório de arquivos!", e);
        }
    }

    public static String readFile(@NonNull File arquivo) {
        val arquivoString = new StringBuilder();
        try(val reader = new BufferedReader(new FileReader(arquivo))) {
            String line;
            while((line = reader.readLine()) != null) {
                arquivoString.append(line)
                        .append(System.lineSeparator());
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return arquivoString.toString();
    }

}
