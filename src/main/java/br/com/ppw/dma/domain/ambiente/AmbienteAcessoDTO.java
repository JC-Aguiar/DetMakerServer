package br.com.ppw.dma.domain.ambiente;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AmbienteAcessoDTO {

    @NotBlank
    String conexao;

    @NotBlank
    String usuario;

    @ToString.Exclude
    String senha;


    public static AmbienteAcessoDTO banco(@NonNull Ambiente ambiente) {
        return new AmbienteAcessoDTO(
            ambiente.getConexaoBanco(),
            ambiente.getUsuarioBanco(),
            ambiente.getSenhaBanco());
    }

    public static AmbienteAcessoDTO ftp(@NonNull Ambiente ambiente) {
        return new AmbienteAcessoDTO(
            ambiente.getConexaoSftp(),
            ambiente.getUsuarioSftp(),
            ambiente.getSenhaSftp());
    }

}
