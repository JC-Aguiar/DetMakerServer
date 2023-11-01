package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.master.MasterResponseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineHistoricoDTO implements MasterResponseDTO {

    @NotNull String pipelineNome;
    @NotNull String pipelineDescricao;
    List<String> jobs = new ArrayList<>();
    Boolean sucesso;

}
