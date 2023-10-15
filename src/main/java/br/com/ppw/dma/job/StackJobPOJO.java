package br.com.ppw.dma.job;

import br.com.ppw.dma.util.ComandoSql;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StackJobPOJO {

    Integer ordem;
    Long id;
    List<String> argumentos = new ArrayList<>();
    List<ComandoSql> queries = new ArrayList<>();
    List<String> cargas = new ArrayList<>();
    Job job;
    JobInfoDTO jobInfo;

}
