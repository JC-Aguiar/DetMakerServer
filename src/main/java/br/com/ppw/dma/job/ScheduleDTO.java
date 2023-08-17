package br.com.ppw.dma.job;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleDTO {

    String nomeArquivo;
    List<PlanilhaSchedule> planilha = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class PlanilhaSchedule {

        String nomePlanilha;
        List<AgendaPOJO> registros = new ArrayList<>();

    }
}
