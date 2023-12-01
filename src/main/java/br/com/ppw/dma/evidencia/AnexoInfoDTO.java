package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.system.Arquivos;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.io.File;
import java.util.List;

public record AnexoInfoDTO(
    @Getter @NotBlank String nome,
    @Getter @NotBlank String tipo,
    @Getter @NotBlank String conteudo) {

    public static AnexoInfoDTO converterExecFile(@NonNull ExecFile execFile) {
        return new AnexoInfoDTO(
            execFile.getArquivoNome(),
            execFile.getTipo().tipo,
            execFile.getArquivo()
        );
    }

    public static List<AnexoInfoDTO> converterExecFile(@NonNull List<ExecFile> execFiles) {
        return execFiles.stream()
            .map(AnexoInfoDTO::converterExecFile)
            .toList();
    }

    public int getPeso() {
        return conteudo.getBytes().length;
    }

}
