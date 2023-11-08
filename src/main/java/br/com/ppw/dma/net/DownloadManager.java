package br.com.ppw.dma.net;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Slf4j
@NoArgsConstructor
public class DownloadManager {

    final List<FileManager> files = new ArrayList<>();


    public DownloadManager(@NonNull List<FileManager> files) {
        this.files.addAll(files);
    }

    public DownloadManager add(@NonNull List<FileManager> newFiles) {
        newFiles.forEach(newFile -> {
            boolean exists = false;
            for(val file : this.files) {
                if(file.reference.equals(newFile.reference)) {
                    file.addFile(newFile);
                    exists = true;
                }
            }
            if(!exists) this.files.add(newFile);
        });
        return this;
    }

    public DownloadManager add(@NonNull FileManager file) {
        this.files.add(file);
        return this;
    }

    public List<File> latestModifiedFiles() {
        return this.files.stream()
            .map(FileManager::latestModified)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }


}
