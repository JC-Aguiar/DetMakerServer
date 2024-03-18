package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.job.JobPreparation;
import br.com.ppw.dma.relatorio.RelatorioInfoDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PipelinePreparation(
    @NotNull Pipeline pipeline,
    @NotNull RelatorioInfoDTO relatorio,
    @NotNull Ambiente ambiente,
    @NotNull List<JobPreparation> jobs) {}
