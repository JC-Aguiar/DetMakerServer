package br.com.ppw.dma.net;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class Uploader {

    @Getter String server;
    @Getter int port;
    String username;
    String password;

    public abstract List<File> upload(String dirRemoto, File...arquivos);
}
