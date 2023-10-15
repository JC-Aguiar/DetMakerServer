package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.master.MasterResponseDTO;
import lombok.*;

import java.io.File;
import java.util.List;

//@Data
@Builder
//@AllArgsConstructor
//@EqualsAndHashCode(callSuper = true)
public record EvidenciaResponseDTO(
    @NonNull String job,
    @NonNull Boolean sucesso,
    String argumentos,
    List<String> queries,
    List<File> tabelas,
    List<File> cargas,
    List<File> logs,
    List<File> saidas)
implements MasterResponseDTO {}


