package br.com.ppw.dma.domain.storage;

import br.com.ppw.dma.config.StorageProperties;
import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.pipeline.execution.PipelineJobCargaDTO;
import br.com.ppw.dma.domain.task.TaskPayloadJobCarga;
import br.com.ppw.dma.exception.StorageException;
import br.com.ppw.dma.exception.StorageFileNotFoundException;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
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
                throw new StorageException("Não foi possível criar diretório do DetMaker: " + pathLocation);
        }
        log.info("Diretório do DetMaker: '{}'.", this.pathLocation.toAbsolutePath());
    }

    @Override
    public File store(MultipartFile file) {
        try {
            if(file.isEmpty()) throw new IllegalArgumentException("O arquivo enviado está vazio!");

            Path destinationFile = doFilePath(file.getOriginalFilename());
            try(InputStream inputStream = file.getInputStream()) {
                return save(inputStream, destinationFile);
            }
        }
        catch(IOException e) {
            throw new StorageException("Erro ao tentar salvar arquivo!", e);
        }
    }

    public File store(PipelineJobCargaDTO file) {
        try {
            if(file.getConteudo().isEmpty()) throw new IllegalArgumentException("O arquivo enviado está vazio!");

            Path destinationFile = doFilePath(file.getNome());
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
            val conteudoBytes = file.getArquivo().getBytes();
            try(InputStream inputStream = new ByteArrayInputStream(conteudoBytes)) {
                return save(inputStream, destinationFile);
            }
        }
        catch(IOException e) {
            throw new StorageException("Erro ao tentar salvar arquivo!", e);
        }
    }

    public File store(TaskPayloadJobCarga file) {
        try {
            if(file.getConteudo().isEmpty()) throw new IllegalArgumentException("O arquivo enviado está vazio!");

            Path destinationFile = doFilePath(file.getNome());
            val conteudoBytes = file.getConteudo().getBytes();
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

    private File save(InputStream inputStream, Path destinationFile) throws IOException {
        if(!destinationFile.getParent().equals(this.pathLocation.toAbsolutePath())) {
            throw new IllegalArgumentException("Não é permitido salvar arquivos fora do diretório configurado!");
        }
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
        if(filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Nome de arquivo inválido: nulo ou vazio");
        }
        var sanitizedFilename = sanitizeFilename(filename);
        return pathLocation.resolve(sanitizedFilename);
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

    public static String sanitizeFilename(String filename) {
        var sanitizedFilename = StringUtils.cleanPath(filename);
        var isInvalidPath = sanitizedFilename == null
            || sanitizedFilename.contains("..")
            || sanitizedFilename.contains("\r")
            || sanitizedFilename.contains("\n")
            || !sanitizedFilename.matches("[a-zA-Z0-9_.-]+");
        if (!isInvalidPath) throw new IllegalArgumentException("Nome de arquivo inválido.");
        return sanitizedFilename;
    }

}
