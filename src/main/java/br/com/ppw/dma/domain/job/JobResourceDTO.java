package br.com.ppw.dma.domain.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.nio.file.Path;

import static br.com.ppw.dma.domain.job.JobResourceProtocolType.FTP;

@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobResourceDTO {

    @NotNull
    @Size(min = 1, max = 9)
    String tipo;

    @NotNull
    @Size(min = 1, max = 255)
    String endereco;

    @Length(min = 1, max = 350)
    String descricao;

    @NotNull
    @Size(min = 1, max = 9)
    String protocolo;


    public JobResourceDTO(@NonNull JobResource resource) {
        this.tipo = (resource.getTipo().name());
        this.endereco = (resource.getEndereco());
        this.descricao = (resource.getDescricao());
        this.protocolo = (resource.getProtocolo().name());
    }

    public JobResource converterParaEntidade() {
        return JobResource.builder()
            .tipo(JobResourceType.identificar(tipo).orElse(null))
            .endereco(endereco)
            .descricao(descricao)
            .protocolo(JobResourceProtocolType.identificar(protocolo).orElse(null))
            .build();
    }

    @JsonIgnore
    public String getAbsolutePath() {
        return JobResourceProtocolType.identificar(protocolo)
            .filter(type -> type == FTP)
            .map(type -> Path.of(endereco).toAbsolutePath().toString())
            .orElse(endereco);
    }

}
