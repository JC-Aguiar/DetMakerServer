package br.com.ppw.dma.domain.task;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.job.JobInfoDTO;
import br.com.ppw.dma.domain.pipeline.execution.PipelineJobInputDTO;
import br.com.ppw.dma.domain.storage.JobPointer;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Valid
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class TaskPayloadJob implements JobPointer {//implements JobExecuter {

	@NotBlank
	String nome;

	@NotBlank
	String descricao;

	@Min(0)
	int ordem;

	@NotBlank
	String comandoExec;

	@Nullable
	String comandoVersao;

	@Builder.Default
	List<TaskPayloadQuery> queriesExec = new ArrayList<>();

	@Nullable
	String dirCargaEnvio;

	@Builder.Default
	List<TaskPayloadJobCarga> cargasEnvio = new ArrayList<>();

	@Builder.Default
	List<String> cargasMascara = new ArrayList<>();

	@Builder.Default
	List<String> logsMascara = new ArrayList<>();

	@Builder.Default
	List<String> remessasMascara = new ArrayList<>();


	public TaskPayloadJob(@NonNull Evidencia evidencia) {
		nome = evidencia.getJobNome();
		descricao = evidencia.getJobDescricao();
		ordem = evidencia.getOrdem();
		comandoExec = evidencia.getComandoExec();
		comandoVersao = evidencia.getComandoVersao();
		queriesExec = evidencia.getQueries()
			.stream()
			.map(TaskPayloadQuery::new)
			.toList();
		dirCargaEnvio = evidencia.getDirCarga();
		cargasEnvio = evidencia.getCargas()
			.stream()
			.map(TaskPayloadJobCarga::new)
			.toList();
		cargasMascara = evidencia.getCargas()
			.stream()
			.map(ExecFile::getMascara)
			.filter(Objects::nonNull)
			.toList();
		logsMascara = evidencia.getLogs()
			.stream()
			.map(ExecFile::getMascara)
			.filter(Objects::nonNull)
			.toList();
		remessasMascara = evidencia.getRemessas()
			.stream()
			.map(ExecFile::getMascara)
			.filter(Objects::nonNull)
			.toList();
	}

	//TODO: mover a definição dos comandos de execução e versão para entidade Job
	public TaskPayloadJob(@NonNull JobInfoDTO jobInfo, @NotNull PipelineJobInputDTO jobInputs) {
		var paramJob = jobInfo.getParametros();
		var paramInputs = jobInputs.getArgumentos().split(" ");
		var variaveis = jobInputs.getVariaveis();

		//Mapeando parâmetros. Chave = índice. Valor = valor declarado na variável (se houver).
		var parametrosPreenchidos = paramJob.stream().collect(
			Collectors.toMap(
				paramJob::indexOf,
				param -> variaveis.getOrDefault(param, "")
		));
		//Atualiza mapa dos parâmetros para cada parâmetro declarado diretamente no JobInput
		for(var i = 0; i < paramInputs.length; i++) {
			if(i < parametrosPreenchidos.size() && !paramInputs[i].isBlank())
				parametrosPreenchidos.put(i, paramInputs[i]);
		}
		jobInputs.setArgumentos(
			String.join(" ", parametrosPreenchidos.values())
		);
		//Aplicando as variáveis nas queries
		jobInputs.getQueries().forEach(queryInput -> {
			var novaSql = FormatString.substituirVariaveis(queryInput.getSql(), variaveis);
			queryInput.setSql(novaSql);
		});

		//Construindo classe
		nome = jobInfo.getNome();
		descricao = jobInfo.getDescricao();
		ordem = jobInputs.getOrdem();
		comandoVersao = "sha256sum %s | cut -d ' ' -f1".formatted(jobInfo.pathToJob());
		comandoExec = "ksh %s %s".formatted(jobInfo.pathToJob(), jobInputs.getArgumentos());
		queriesExec = jobInputs.getQueries()
			.stream()
			.map(TaskPayloadQuery::DML)
			.toList();
		cargasEnvio = jobInputs.getCargas()
			.stream()
			.map(TaskPayloadJobCarga::new)
			.toList();
		cargasMascara = jobInfo.getMascaraEntrada()
			.stream()
			.map(mascara -> Optional.ofNullable(jobInfo.getDiretorioEntrada())
				.map(dir -> dir.endsWith("/") ? dir : dir+"/")
				.map(dir -> dir + mascara.trim())
				.orElseGet(() -> ""))
			.toList();
		remessasMascara = jobInfo.getMascaraSaida()
			.stream()
			.map(mascara -> Optional.ofNullable(jobInfo.getDiretorioSaida())
				.map(dir -> dir.endsWith("/") ? dir : dir+"/")
				.map(dir -> dir + mascara.trim())
				.orElseGet(() -> ""))
			.toList();
		logsMascara = jobInfo.getMascaraLog()
			.stream()
			.map(mascara -> Optional.ofNullable(jobInfo.getDiretorioLog())
				.map(dir -> dir.endsWith("/") ? dir : dir+"/")
				.map(dir -> dir + mascara.trim())
				.orElseGet(() -> ""))
			.toList();
		dirCargaEnvio = Optional.ofNullable(jobInfo.getDiretorioEntrada())
			.map(dir -> dir.endsWith("/") ? dir : dir+"/")
			.orElseGet(() -> "");
	}


	/**
	 * Identifica, através do name, e unifica {@link Job}s com seus respectivos inputs
	 * ({@link PipelineJobInputDTO}).
	 * @param infoDtos {@link Collection} {@link JobInfoDTO}
	 * @param inputDtos {@link Collection} {@link PipelineJobInputDTO}
	 * @return {@link Collection} {@link TaskPayloadJob} contendo a unificação
	 */
	public static List<TaskPayloadJob> matchAndMerge(
		@NonNull Collection<JobInfoDTO> infoDtos,
		@NonNull Collection<PipelineJobInputDTO> inputDtos) {

		log.info("Agrupando Jobs x Inputs.");
		return infoDtos.stream()
			.map(job -> TaskPayloadJob.matchAndMerge(job, inputDtos))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.toList();
	}

	/**
	 * Método de unificação usado no {@link TaskPayloadJob#matchAndMerge(Collection, Collection)}.
	 * @param infoDto {@link JobInfoDTO}
	 * @param execDtos {@link PipelineJobInputDTO}
	 * @return {@link Optional} {@link TaskPayloadJob} contendo ou não a unificação
	 * @see TaskPayloadJob#matchAndMerge(Collection, Collection)
	 */
	public static Optional<TaskPayloadJob> matchAndMerge(
		@NonNull JobInfoDTO infoDto,
		@NonNull Collection<PipelineJobInputDTO> execDtos) {

		return execDtos.stream()
			.filter(dto -> infoDto.getId().equals(dto.getId()))
			.findFirst()
			.map(dto -> new TaskPayloadJob(infoDto, dto));
	}

	@Override
	@JsonIgnore
	public String pathToJob() {
		return comandoExec;
	}

	@Override
	@JsonIgnore
	public List<String> pathLog() {
		return logsMascara;
	}

	@Override
	@JsonIgnore
	public List<String> pathSaida() {
		return remessasMascara;
	}

	@Override
	@JsonIgnore
	public List<String> pathEntrada() {
		return cargasMascara;
//		return cargasEnvio.stream()
//			.map(TaskPayloadJobCarga::getNome)
//			.map(cargaNome -> dirCargaEnvio + cargaNome)
//			.toList();
	}

	@Override
	@JsonIgnore
	public List<String> getAllTabelas() {
		return queriesExec.stream()
			.map(TaskPayloadQuery::getQuery)
			.toList();
	}

//	public void aplicarConfiguracoes(@NonNull Map<String, String> configuracoes) {
//		configuracoes.forEach((key, value) -> jobInputs.getVariaveis().put(key, value));
//		aplicarConfiguracoes();
//	}

//	private void prepararDados(@NonNull JobInfoDTO jobInfo, @NonNull PipelineJobInputDTO jobInputs) {
//		var parametros = jobInfo.getParametros();
//		var paramInputs = jobInputs.getArgumentos().split(" ");
//		var variaveis = jobInputs.getVariaveis();
//
//		//Mapeando parâmetros. Chave = índice. Valor = valor declarado na variável (se houver).
//		var parametrosPreenchidos = parametros.stream().collect(
//			Collectors.toMap(
//				parametros::indexOf,
//				param -> variaveis.getOrDefault(param, "")
//		));
//		//Atualiza mapa dos parâmetros para cada parâmetro declarado diretamente no JobInput
//		for(var i = 0; i < paramInputs.length; i++) {
//			if(i < parametrosPreenchidos.size() && !paramInputs[i].isBlank())
//				parametrosPreenchidos.put(i, paramInputs[i]);
//		}
//		//TODO: usar?
//		//Identifica parâmetros pendentes
////		var parametrosPendentes = parametrosPreenchidos.keySet()
////			.stream()
////			.filter(chave -> parametrosPreenchidos.get(chave).isBlank())
////			.map(chave -> infoDto.getDescricaoParametros().get(chave))
////			.filter(paramDesc -> !paramDesc.toLowerCase().contains("opcional"))
////			.filter(paramDesc -> !paramDesc.toLowerCase().contains("facultativo"))
////			.toList();
////
////		if(parametrosPendentes.size() > 0) {
////			throw new RuntimeException("Parâmetros obrigatórios incompletos para Job '"
////				+ infoDto.getNome() + "': "
////				+ String.join(", ", parametrosPendentes));
////		}
//		jobInputs.setArgumentos(
//			String.join(" ", parametrosPreenchidos.values())
//		);
//		//Aplicando as variáveis nas queries
//		jobInputs.getQueries().forEach(queryInput -> {
//			var novaSql = FormatString.substituirVariaveis(queryInput.getSql(), variaveis);
//			queryInput.setSql(novaSql);
//		});
//	}

}
