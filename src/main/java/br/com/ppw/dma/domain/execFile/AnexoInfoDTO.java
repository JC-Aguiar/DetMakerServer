package br.com.ppw.dma.domain.execFile;

import br.com.ppw.dma.util.FormatString;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

import java.util.List;
import java.util.Optional;

@Builder
public record AnexoInfoDTO(
    String nome,
    @NonNull String tipo,
    @NonNull String mascara,
    @ToString.Exclude String conteudo,
    String inconformidade)
{

    //TODO: javadoc
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
            .mascara(
                Optional.ofNullable(execFile.getMascara())
                .orElse(""))
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

    @ToString.Include(name = "conteudo")
    private String getResumoConteudo() {
        val tamanho = FormatString.contarSubstring(conteudo, "\n");
        return String.format("[linhas=%d, peso=%dKbs]", tamanho, getPeso());
    }

}
