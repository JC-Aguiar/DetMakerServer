//package br.com.ppw.dma.domain.storage;
//
//import br.com.ppw.dma.exception.StorageFileNotFoundException;
//import br.com.ppw.dma.domain.master.MasterSummary;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.owasp.encoder.Encode;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.Resource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.util.StringUtils;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.util.UriUtils;
//
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.function.Predicate;
//
//@Slf4j
//@RestController
//@RequestMapping("storage")
//public class FileSystemController {
//
//    private final FileSystemService storageService;
//
//    @Autowired
//    public FileSystemController(FileSystemService storageService) {
//        this.storageService = storageService;
//    }
//
//
//    @GetMapping("/files/{filename:.+}")
//    @ResponseBody
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//        var sanitizedFilename = FileSystemService.sanitizeFilename(filename);
//        var file = storageService.loadAsResource(sanitizedFilename);
//        if(file == null || file.getFilename() == null || !file.exists()) {
//            return ResponseEntity.notFound().build();
//        }
//        // Escapar o nome do arquivo para cabeçalhos HTTP
//        var headerFilename = sanitizedFilename
//            .replaceAll("[^a-zA-Z0-9._-]", "_") // Substituir caracteres não permitidos
//            .replaceAll("[\"';]", "_");         // Bloquear aspas e ponto-e-vírgula
//        var encodedHeaderFilename = UriUtils.encode(headerFilename, StandardCharsets.UTF_8);
//        var headerContentDisposition = "attachment; filename*=UTF-8''%s".formatted(encodedHeaderFilename);
//
//        return ResponseEntity.ok()
//            .contentType(MediaType.APPLICATION_OCTET_STREAM)
//            .header(HttpHeaders.CONTENT_DISPOSITION, headerContentDisposition)
//            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
//            .header(HttpHeaders.PRAGMA, "no-cache")
//            .header(HttpHeaders.EXPIRES, "0")
//            .body(file);
//    }
//
//    //TODO: mover validações para camada de serviço
//    @PostMapping()
//    public ResponseEntity<MasterSummary<String>> upload(@RequestParam("files") List<MultipartFile> files) {
//        val summary = new MasterSummary<String>();
//        files.forEach(file -> Optional.ofNullable(file.getOriginalFilename())
//            .map(FileSystemService::sanitizeFilename)
//            .filter(name -> Objects.equals(file.getContentType(), "text/plain"))
//            .filter(this::isValidExtension)
//            .ifPresentOrElse(
//                name -> {
//                    try {
//                        storageService.store(file);
//                        summary.save(name);
//                    }
//                    catch(Exception e) {
//                        log.error(e.getMessage());
//                        summary.fail(name, e.getMessage());
//                    }
//                },
//                () -> summary.fail(file.getOriginalFilename(), "Nome/tipo de arquivo inválido.")
//        ));
//        return MasterSummary.toResponseEntity(summary);
//    }
//
//    @ExceptionHandler(StorageFileNotFoundException.class)
//    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
//        return ResponseEntity.notFound().build();
//    }
//
//    private boolean isValidExtension(String fileName) {
//        var lowerCaseFileName = fileName.toLowerCase();
//        return lowerCaseFileName.endsWith(".txt") ||
//            lowerCaseFileName.endsWith(".log") ||
//            lowerCaseFileName.endsWith(".gpg");
//    }
//
//}
