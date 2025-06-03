package br.com.ppw.dma.domain.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateJobDTO {

    @NotBlank
    @Size(max = 100)
    String nome;

    @Size(max = 500)
    String descricao;

    @Size(max = 100)
    String caminhoExec;

    List<String> parametros = new ArrayList<>();

    List<String> descricaoParametros = new ArrayList<>();

    @Size(max = 100)
    String diretorioEntrada;

    List<String> mascaraEntrada = new ArrayList<>();

    @Size(max = 100)
    String diretorioSaida;

    List<String> mascaraSaida = new ArrayList<>();

    @Size(max = 100)
    String diretorioLog;

    List<String> mascaraLog = new ArrayList<>();

}
