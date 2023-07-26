package br.com.ppw.dma.util;

import javax.validation.constraints.NotBlank;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class Download {

    static int count = 0;

    public static byte[] from(@NotBlank String urlString) throws IOException {
        //TODO: this method is not working. System report "access denied"
        final URL url = new URL(urlString);
        final String filePath = Paths.get("/temp/").toAbsolutePath().toString();
        final String fileName = filePath + String.format("file%02d", ++count);
        try(final BufferedInputStream in = new BufferedInputStream(url.openStream());
            final FileOutputStream out = new FileOutputStream(fileName)) {
            final byte[] file = in.readAllBytes();
            out.write(file);
            System.out.printf("File %s saved in path %s", fileName, filePath);
            return file;
        } catch (IOException e) {
            System.out.println("Error while downloading url file: " + e.getLocalizedMessage());
            return new byte[0];
        }
    }

}
