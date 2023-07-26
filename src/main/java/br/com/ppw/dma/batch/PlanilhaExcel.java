package br.com.ppw.dma.batch;

import br.com.ppw.dma.agenda.AgendaDTO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlanilhaExcel {

    private final String nome;
    private final List<AgendaDTO> agendaDTOS = new ArrayList<>();

    public PlanilhaExcel(String nome) {
        this.nome = nome;
    }

    public PlanilhaExcel(String nome, List<AgendaDTO> agendaDTOS) {
        this.nome = nome;
        this.agendaDTOS.addAll(agendaDTOS);
    }

    public PlanilhaExcel addCampoSchedule(AgendaDTO dto) {
        this.agendaDTOS.add(dto);
        return this;
    }

    public PlanilhaExcel addCampoSchedule(List<AgendaDTO> agendaDTOS) {
        this.agendaDTOS.addAll(agendaDTOS);
        return this;
    }

}
