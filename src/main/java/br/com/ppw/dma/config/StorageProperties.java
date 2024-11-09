package br.com.ppw.dma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;


@ConfigurationProperties("storage")
public class StorageProperties {

    //Configuração do diretório onde será armazenado os arquivos
    private String location; // = "safe-dir";


    public String getLocation() {
        return Optional.ofNullable(location).orElse("safe-dir");
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
