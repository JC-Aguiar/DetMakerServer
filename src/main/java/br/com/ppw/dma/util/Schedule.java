package br.com.ppw.dma.util;

import br.com.ppw.dma.job.JobSchedulePOJO;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Schedule {

    String nomeArquivo;
    List<PlanilhaSchedule> planilha = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class PlanilhaSchedule {

        String nomePlanilha;
        List<JobSchedulePOJO> registros = new ArrayList<>();

    }
}
