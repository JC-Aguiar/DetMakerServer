package br.com.ppw.dma.domain.execFile;

import lombok.NonNull;

import java.util.List;

public record AnexoInfoDTO(
    String nome,
    @NonNull String tipo,
    String conteudo,
    String inconformidade) {

    //TODO: auhssauhsaus
    public static AnexoInfoDTO converterExecFile(@NonNull ExecFile execFile) {
        return new AnexoInfoDTO(
            execFile.getArquivoNome() == null ? "" : execFile.getArquivoNome(),
            execFile.getTipo().tipo,
            execFile.getArquivo() == null ? "" : execFile.getArquivo(),
            execFile.getInconformidade() == null ? "" : execFile.getInconformidade());
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
