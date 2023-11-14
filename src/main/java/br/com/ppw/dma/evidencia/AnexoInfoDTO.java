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

    public static final String TIPO_CARGA = "Arquivos de carga consumidos pelo Job";
    public static final String TIPO_PRODUTO = "Arquivos produzidos ao término do Job";
    public static final String TIPO_LOG = "Logs produzidos pela execução do Job";


    public static AnexoInfoDTO tipoCarga(@NonNull File arquivo) {
        val conteudo = Arquivos.lerArquivo(arquivo);
        return new AnexoInfoDTO(arquivo.getName(), TIPO_CARGA, conteudo);
    }

    public static AnexoInfoDTO tipoCarga(@NotBlank String nome, @NotBlank String conteudo) {
        return new AnexoInfoDTO(nome, TIPO_CARGA, conteudo);
    }

    public static List<AnexoInfoDTO> tipoCarga(@NonNull List<ExecFile> cargas) {
        return cargas.stream()
            .map(carga -> AnexoInfoDTO.tipoCarga(carga.getArquivoNome(), carga.getArquivo()))
            .toList();
    }

    public static AnexoInfoDTO tipoProduto(@NonNull File arquivo) {
        val conteudo = Arquivos.lerArquivo(arquivo);
        return new AnexoInfoDTO(arquivo.getName(), TIPO_PRODUTO, conteudo);
    }

    public static AnexoInfoDTO tipoProduto(@NotBlank String nome, @NotBlank String conteudo) {
        return new AnexoInfoDTO(nome, TIPO_PRODUTO, conteudo);
    }

    public static List<AnexoInfoDTO> tipoProduto(@NonNull List<ExecFile> produtos) {
        return produtos.stream()
            .map(prod -> AnexoInfoDTO.tipoProduto(prod.getArquivoNome(), prod.getArquivo()))
            .toList();
    }

    public static AnexoInfoDTO tipoLog(@NonNull File arquivo) {
        val conteudo = Arquivos.lerArquivo(arquivo);
        return new AnexoInfoDTO(arquivo.getName(), TIPO_LOG, conteudo);
    }

    public static AnexoInfoDTO tipoLog(@NotBlank String nome, @NotBlank String conteudo) {
        return new AnexoInfoDTO(nome, TIPO_LOG, conteudo);
    }

    public static List<AnexoInfoDTO> tipoLog(@NonNull List<ExecFile> logs) {
        return logs.stream()
            .map(log -> AnexoInfoDTO.tipoLog(log.getArquivoNome(), log.getArquivo()))
            .toList();
    }

    public int getPeso() {
        return conteudo.getBytes().length;
    }

}
