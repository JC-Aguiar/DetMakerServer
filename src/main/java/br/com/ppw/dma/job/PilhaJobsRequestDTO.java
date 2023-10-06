package br.com.ppw.dma.job;

import br.com.ppw.dma.job.ComandoSql;
import br.com.ppw.dma.master.MasterDtoRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PilhaJobsRequestDTO implements MasterDtoRequest {

    Long jobId;
    Integer ordem;
    String argumentos;
    List<ComandoSql> queries = new ArrayList<>();
    List<File> cargas = new ArrayList<>();

}
