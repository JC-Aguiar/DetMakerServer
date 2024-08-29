package br.com.ppw.dma.domain.pipeline;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.job.JobPreparation;
import br.com.ppware.api.MassaPreparada;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PipelinePreparation(
	@NotNull Pipeline pipeline,
	@NotNull Ambiente ambiente,				//TODO: mover para dentro de Atividade
	@NotNull List<JobPreparation> jobs,
	List<MassaPreparada> massas) {

}
