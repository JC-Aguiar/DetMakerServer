package br.com.ppw.dma.ambiente;

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
public class AmbienteInfoDTO {

    @NotNull Long id;
    @NotNull String nome;
    AmbienteAcessoDTO banco;
    AmbienteAcessoDTO ftp;

    public AmbienteInfoDTO(@NotBlank String nome) {
        this.nome = nome;
    }

    public AmbienteInfoDTO(@NonNull Ambiente ambiente) {
        this.id = ambiente.getId();
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
