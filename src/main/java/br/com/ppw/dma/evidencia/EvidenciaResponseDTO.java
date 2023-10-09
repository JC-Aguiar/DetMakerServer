package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.master.MasterDtoResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.List;
import java.util.Map;

//@Data
@Builder
//@AllArgsConstructor
//@EqualsAndHashCode(callSuper = true)
public record EvidenciaResponseDTO(
    @NonNull String job,
    @NonNull Boolean sucesso,
    String argumentos,
    List<String> query,
    List<File> tabelas,
    List<File> cargas,
    List<File> logs,
    List<File> saidas)
implements MasterDtoResponse {}


