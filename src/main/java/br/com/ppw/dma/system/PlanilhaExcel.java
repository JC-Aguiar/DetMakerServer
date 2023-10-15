package br.com.ppw.dma.system;

import br.com.ppw.dma.job.JobSchedulePOJO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PlanilhaExcel {

    private final String nome;
    private final List<JobSchedulePOJO> jobsPOJO = new ArrayList<>();

    public PlanilhaExcel(String nome) {
        this.nome = nome;
    }

    public PlanilhaExcel(String nome, List<JobSchedulePOJO> jobsPOJO) {
        this.nome = nome;
        this.jobsPOJO.addAll(jobsPOJO);
    }

    public PlanilhaExcel addCampoSchedule(JobSchedulePOJO dto) {
        this.jobsPOJO.add(dto);
        return this;
    }

    public PlanilhaExcel addCampoSchedule(List<JobSchedulePOJO> jobPojo) {
        this.jobsPOJO.addAll(jobPojo);
        return this;
    }

}
