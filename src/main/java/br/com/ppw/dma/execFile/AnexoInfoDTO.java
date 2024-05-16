package br.com.ppw.dma.execFile;

import lombok.NonNull;

import java.util.List;

public record AnexoInfoDTO(
    @NonNull String nome,
    @NonNull String tipo,
    @NonNull String conteudo,
    @NonNull String inconformidade) {

    //TODO: auhssauhsaus
    public static AnexoInfoDTO converterExecFile(@NonNull ExecFile execFile) {
        return new AnexoInfoDTO(
            execFile.getArquivoNome(),
            execFile.getTipo().tipo,
            execFile.getArquivo(),
            execFile.getInconformidade());
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
