package br.com.ppw.dma.domain.storage;

import br.com.ppw.dma.exception.StorageFileNotFoundException;
import br.com.ppw.dma.domain.master.MasterSummary;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@RestController
@RequestMapping("storage")
public class FileSystemController {

    private final StorageService storageService;


    @Autowired
    public FileSystemController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        var sanitizedFilename = StringUtils.cleanPath(filename);
        if(sanitizedFilename.contains("..") || sanitizedFilename.contains("\r") || sanitizedFilename.contains("\n")) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }
        Resource file = storageService.loadAsResource(sanitizedFilename);
        if(file == null || file.getFilename() == null) {
            return ResponseEntity.notFound().build();
        }
        var headerValue = "attachment; filename=\"%s\"".formatted(
            sanitizedFilename.replace("\"", "\\\"")
        );;
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
            .body(file);
    }

    //TODO: mover validações para camada de serviço
    @PostMapping()
    public ResponseEntity<MasterSummary<String>> upload(@RequestParam("files") List<MultipartFile> files) {
        val summary = new MasterSummary<String>();
        files.forEach(file -> Optional.ofNullable(file.getOriginalFilename())
            .map(StringUtils::cleanPath)
            .filter(name -> !name.contains(".."))
            .filter(name -> Objects.equals(file.getContentType(), "text/plain"))
            .filter(this::isValidExtension)
            .ifPresentOrElse(
                name -> {
                    try {
                        storageService.store(file);
                        summary.save(name);
                    }
                    catch(Exception e) {
                        log.error(e.getMessage());
                        summary.fail(name, e.getMessage());
                    }
                },
                () -> summary.fail(file.getOriginalFilename(), "Nome/tipo de arquivo inválido.")
        ));
        return MasterSummary.toResponseEntity(summary);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    private boolean isValidExtension(String fileName) {
        var lowerCaseFileName = fileName.toLowerCase();
        return lowerCaseFileName.endsWith(".txt") ||
            lowerCaseFileName.endsWith(".log") ||
            lowerCaseFileName.endsWith(".gpg");
    }

}
