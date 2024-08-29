package br.com.ppw.dma.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties("storage")
public class StorageProperties {

    //Configuração do diretório onde será armazenado os arquivos
    private String location = "upload-dir";


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
