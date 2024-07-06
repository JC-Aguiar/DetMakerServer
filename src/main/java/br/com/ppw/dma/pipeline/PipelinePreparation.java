package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.job.JobPreparation;
import br.com.ppw.dma.relatorio.AtividadeInfoDTO;
import br.com.ppware.api.MassaTabelaDTO;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PipelinePreparation(
	@NotNull Pipeline pipeline,
	@NotNull AtividadeInfoDTO relatorio,
	@NotNull Ambiente ambiente,
	@NotNull List<JobPreparation> jobs,
	@NotNull Map<String, String> preQueries,
	@NotNull Map<String, String> posQueries) {

}
