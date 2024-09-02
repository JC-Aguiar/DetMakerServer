package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.job.JobInfoDTO;
import br.com.ppw.dma.domain.pipeline.execution.PipelineJobInputDTO;
import br.com.ppw.dma.util.FormatString;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueuePayloadJob {
	
    @NotNull JobInfoDTO jobInfo;
    @NotNull PipelineJobInputDTO jobInputs;


	/**
	 * Identifica, através do name, e unifica {@link Job}s com seus respectivos inputs
	 * ({@link PipelineJobInputDTO}).
	 * @param infoDtos {@link Collection} {@link JobInfoDTO}
	 * @param inputDtos {@link Collection} {@link PipelineJobInputDTO}
	 * @return {@link Collection} {@link QueuePayloadJob} contendo a unificação
	 */
	public static List<QueuePayloadJob> match(
		@NonNull Collection<JobInfoDTO> infoDtos,
		@NonNull Collection<PipelineJobInputDTO> inputDtos) {

		log.info("Agrupando Jobs x Inputs.");
		return infoDtos.stream()
			.map(job -> QueuePayloadJob.match(job, inputDtos))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.toList();
	}

	/**
	 * Método de unificação usado no {@link QueuePayloadJob#match(Collection, Collection)}.
	 * @param infoDto {@link JobInfoDTO}
	 * @param execDtos {@link PipelineJobInputDTO}
	 * @return {@link Optional} {@link QueuePayloadJob} contendo ou não a unificação
	 * @see QueuePayloadJob#match(Collection, Collection)
	 */
	public static Optional<QueuePayloadJob> match(
		@NonNull JobInfoDTO infoDto,
		@NonNull Collection<PipelineJobInputDTO> execDtos) {

		return execDtos.stream()
			.filter(dto -> infoDto.getId().equals(dto.getId()))
			.findFirst()
			.map(dto -> new QueuePayloadJob(infoDto, dto));
	}

	public void aplicarConfiguracoes(@NonNull Map<String, String> configuracoes) {
		configuracoes.entrySet().forEach(
			conf -> jobInputs.getVariaveis().put(
				conf.getKey(),
				conf.getValue()
		));
		aplicarConfiguracoes();
	}

	public void aplicarConfiguracoes() {
		var parametros = jobInfo.getParametros();
		var paramInputs = jobInputs.getArgumentos().split(" ");
		var variaveis = jobInputs.getVariaveis();

		//Mapeando parâmetros. Chave = índice. Valor = valor declarado na variável (se houver).
		var parametrosPreenchidos = parametros.stream().collect(
			Collectors.toMap(
				parametros::indexOf,
				param -> variaveis.getOrDefault(param, "")
		));
		//Atualiza mapa dos parâmetros para cada parâmetro declarado diretamente no JobInput
		for(var i = 0; i < paramInputs.length; i++) {
			if(i < parametrosPreenchidos.size() && !paramInputs[i].isBlank())
				parametrosPreenchidos.put(i, paramInputs[i]);
		}
		//TODO: usar?
		//Identifica parâmetros pendentes
//		var parametrosPendentes = parametrosPreenchidos.keySet()
//			.stream()
//			.filter(chave -> parametrosPreenchidos.get(chave).isBlank())
//			.map(chave -> infoDto.getDescricaoParametros().get(chave))
//			.filter(paramDesc -> !paramDesc.toLowerCase().contains("opcional"))
//			.filter(paramDesc -> !paramDesc.toLowerCase().contains("facultativo"))
//			.toList();
//
//		if(parametrosPendentes.size() > 0) {
//			throw new RuntimeException("Parâmetros obrigatórios incompletos para Job '"
//				+ infoDto.getNome() + "': "
//				+ String.join(", ", parametrosPendentes));
//		}
		jobInputs.setArgumentos(
			String.join(" ", parametrosPreenchidos.values())
		);
		//Aplicando as variáveis nas queries
		jobInputs.getQueries().forEach(queryInput -> {
			var novaSql = FormatString.substituirVariaveis(queryInput.getSql(), variaveis);
			queryInput.setSql(novaSql);
		});
	}

}
