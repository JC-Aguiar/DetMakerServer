package br.com.ppw.dma.job;

import br.com.ppw.dma.util.FormatString;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record JobPreparation(
    @NotNull Job job,
    @NotNull JobExecuteDTO jobInputs) {


	//TODO: testar!
	public void aplicarConfiguracoes(@NonNull Map<String, String> configuracoes) {
		jobInputs.getVariaveis().putAll(configuracoes);
		aplicarConfiguracoes();
	}

	public void aplicarConfiguracoes() {
		var infoDto = JobInfoDTO.converterJob(job);
		var variaveis = jobInputs.getVariaveis();

		var parametros = infoDto.getParametros()
			.stream()
			.collect(Collectors.toMap(
				paramMask -> infoDto.getParametros().indexOf(paramMask),
				paramMask -> variaveis.getOrDefault(paramMask, ""))
			);
		var paramInputs = jobInputs.getArgumentos().split(" ");
		for(var i = 0; i < paramInputs.length; i++) {
			if(i < parametros.size())
				parametros.put(i, paramInputs[i]);
		}
		var parametrosPendentes = parametros.keySet()
			.stream()
			.filter(chave -> parametros.get(chave).isBlank())
			.map(chave -> infoDto.getDescricaoParametros().get(chave))
			.filter(paramDesc -> !paramDesc.toLowerCase().contains("opcional"))
			.filter(paramDesc -> !paramDesc.toLowerCase().contains("facultativo"))
			.toList();

		//TODO: criar exception
		if(parametrosPendentes.size() > 0) {
			throw new RuntimeException("Parâmetros obrigatórios incompletos para Job '"
				+ infoDto.getNome() + "': "
				+ String.join(", ", parametrosPendentes));
		}
		jobInputs.setArgumentos(String.join(" ", paramInputs));
		jobInputs.getQueries().forEach(queryDto -> queryDto.setSql(
			FormatString.substituirVariaveis(
				queryDto.getSql(),
				jobInputs.getVariaveis())
		));
	}

}
