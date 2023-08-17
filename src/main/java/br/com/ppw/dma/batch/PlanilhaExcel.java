package br.com.ppw.dma.batch;

import br.com.ppw.dma.job.AgendaPOJO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlanilhaExcel {

    private final String nome;
    private final List<AgendaPOJO> agendaPOJOS = new ArrayList<>();

    public PlanilhaExcel(String nome) {
        this.nome = nome;
    }

    public PlanilhaExcel(String nome, List<AgendaPOJO> agendaPOJOS) {
        this.nome = nome;
        this.agendaPOJOS.addAll(agendaPOJOS);
    }

    public PlanilhaExcel addCampoSchedule(AgendaPOJO dto) {
        this.agendaPOJOS.add(dto);
        return this;
    }

    public PlanilhaExcel addCampoSchedule(List<AgendaPOJO> agendaPOJOS) {
        this.agendaPOJOS.addAll(agendaPOJOS);
        return this;
    }

}
