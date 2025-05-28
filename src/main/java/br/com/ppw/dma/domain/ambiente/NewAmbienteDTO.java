package br.com.ppw.dma.domain.ambiente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewAmbienteDTO {

    @NotBlank String nome;
    @NotNull AmbienteAcessoDTO banco;
    @NotNull AmbienteAcessoDTO ftp;


    public NewAmbienteDTO(@NonNull Ambiente ambiente) {
        this.nome = ambiente.getNome();
        this.banco = new AmbienteAcessoDTO(
            ambiente.getConexaoBanco(),
            ambiente.getUsuarioBanco(),
            null
        );
        this.ftp = new AmbienteAcessoDTO(
            ambiente.getConexaoSftp(),
            ambiente.getUsuarioSftp(),
            null
        );
    }

}
