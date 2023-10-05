package br.com.ppw.dma.system;

import br.com.ppw.dma.job.JobPOJO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlanilhaExcel {

    private final String nome;
    private final List<JobPOJO> jobsPOJO = new ArrayList<>();

    public PlanilhaExcel(String nome) {
        this.nome = nome;
    }

    public PlanilhaExcel(String nome, List<JobPOJO> jobsPOJO) {
        this.nome = nome;
        this.jobsPOJO.addAll(jobsPOJO);
    }

    public PlanilhaExcel addCampoSchedule(JobPOJO dto) {
        this.jobsPOJO.add(dto);
        return this;
    }

    public PlanilhaExcel addCampoSchedule(List<JobPOJO> jobPOJOS) {
        this.jobsPOJO.addAll(jobPOJOS);
        return this;
    }

}
