package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.job.JobPreparation;
import br.com.ppw.dma.relatorio.AtividadeInfoDTO;
import br.com.ppware.api.MassaPreparada;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PipelinePreparation(
	@NotNull Pipeline pipeline,
	@NotNull AtividadeInfoDTO relatorio,	//TODO: trocar por para Entidade
	@NotNull Ambiente ambiente,				//TODO: mover para dentro de Atividade
	@NotNull List<JobPreparation> jobs,
	List<MassaPreparada> massas) {

}
