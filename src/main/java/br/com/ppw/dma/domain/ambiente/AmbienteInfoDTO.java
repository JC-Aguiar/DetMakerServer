package br.com.ppw.dma.domain.ambiente;

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

    Long id;
    String nome;
    AmbienteAcessoDTO banco;
    AmbienteAcessoDTO ftp;


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
