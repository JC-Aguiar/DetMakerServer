package br.com.ppw.dma.domain.execFile;

import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

@Builder
public record AnexoInfoDTO(
    String nome,
    @NonNull String tipo,
    @NonNull String mascara,
    String conteudo,
    String inconformidade) {

    //TODO: auhssauhsaus
    public static AnexoInfoDTO converterExecFile(@NonNull ExecFile execFile) {
        return AnexoInfoDTO.builder()
            .nome(
                Optional.ofNullable(execFile.getArquivoNome())
                .orElse(""))
            .conteudo(
                Optional.ofNullable(execFile.getArquivo())
                .orElse(""))
            .inconformidade(
                Optional.ofNullable(execFile.getInconformidade())
                .orElse(""))
            .mascara(execFile.getMascara())
            .tipo(execFile.getTipo().tipo)
            .build();
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
