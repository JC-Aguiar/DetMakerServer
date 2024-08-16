package br.com.ppw.dma.job;

import br.com.ppw.dma.util.FormatString;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record JobPreparation(
    @NotNull Job job,
    @NotNull JobExecuteDTO jobInputs) {


	public static Optional<JobPreparation> match(
		@NonNull Job job,
		@NonNull Collection<JobExecuteDTO> execDto) {

		return execDto.parallelStream()
			.filter(dto -> job.getId().equals(dto.getId()))
			.findFirst()
			.map(dto -> new JobPreparation(job, dto));
	}

	public void aplicarConfiguracoes(@NonNull Map<String, String> configuracoes) {
		configuracoes.entrySet()
			.parallelStream()
			.forEach(conf -> jobInputs.getVariaveis().putIfAbsent(
				conf.getKey(),
				conf.getValue()
			));
		aplicarConfiguracoes();
	}

	public void aplicarConfiguracoes() {
		var parametros = FormatString.dividirValores(job.getParametros());
		var paramInputs = jobInputs.getArgumentos().split(" ");
		var variaveis = jobInputs.getVariaveis();

		//Mapeando parâmetros. Chave = índice. Valor = valor declarado na variável (se houver).
		var parametrosPreenchidos = parametros.stream().collect(
			Collectors.toMap(
				parametros::indexOf,
				param -> variaveis.getOrDefault(param, ""))
		);
		//Atualiza mapa dos parâmetros para cada parâmetro declarado diretamente no JobInput
		for(var i = 0; i < paramInputs.length; i++) {
			if(i < parametrosPreenchidos.size() && !paramInputs[i].isBlank())
				parametrosPreenchidos.put(i, paramInputs[i]);
		}
		//Identifica parâmetros pendentes
//		var parametrosPendentes = parametrosPreenchidos.keySet()
//			.stream()
//			.filter(chave -> parametrosPreenchidos.get(chave).isBlank())
//			.map(chave -> infoDto.getDescricaoParametros().get(chave))
//			.filter(paramDesc -> !paramDesc.toLowerCase().contains("opcional"))
//			.filter(paramDesc -> !paramDesc.toLowerCase().contains("facultativo"))
//			.toList();
//
//		//TODO: criar exception
//		if(parametrosPendentes.size() > 0) {
//			throw new RuntimeException("Parâmetros obrigatórios incompletos para Job '"
//				+ infoDto.getNome() + "': "
//				+ String.join(", ", parametrosPendentes));
//		}
		jobInputs.setArgumentos(
			String.join(" ", parametrosPreenchidos.values())
		);
		//Aplicando as variáveis nas queries
		jobInputs.getQueries().forEach(queryDto -> queryDto.setSql(
			FormatString.substituirVariaveis(
				queryDto.getSql(),
				jobInputs.getVariaveis())
		));
	}

}
