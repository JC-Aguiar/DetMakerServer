package br.com.ppw.dma.domain.storage;

import br.com.ppw.dma.exception.StorageFileNotFoundException;
import br.com.ppw.dma.domain.master.MasterSummary;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
        Resource file = storageService.loadAsResource(filename);
        if(file == null)
            return ResponseEntity.notFound().build();

        val headerValues = "attachment; filename=\"" + file.getFilename() + "\"";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, headerValues)
            .body(file);
    }

    @PostMapping()
    public ResponseEntity<MasterSummary<String>> upload(@RequestParam("files") List<MultipartFile> files) {
        val summary = new MasterSummary<String>();
        for(val file : files) {
            try {
                storageService.store(file);
                summary.save(file.getOriginalFilename());
            }
            catch(Exception e) {
                log.error(e.getMessage());
                summary.fail(file.getOriginalFilename(), e.getMessage());
            }
        }
        return MasterSummary.toResponseEntity(summary);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
