package br.com.ppw.dma.domain.task;

import br.com.ppw.dma.domain.execQuery.ExecQuery;
import br.com.ppw.dma.domain.jobQuery.JobQuery;
import br.com.ppw.dma.domain.master.SqlSintaxe;
import br.com.ppw.dma.domain.pipeline.execution.PipelineQueryInputDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Valid
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class TaskPayloadQuery {

	@NotBlank String nome;
	@NotBlank String descricao;
	@NotBlank String query;
	@Nullable SqlSintaxe.QueryMethod method; //TODO: remover?


	public TaskPayloadQuery(@NonNull ExecQuery execQuery) {
		nome = execQuery.getQueryNome();
		descricao = execQuery.getQueryDescricao();
		query = execQuery.getQuery();
		method = null;
	}

	public TaskPayloadQuery(@NonNull JobQuery jobQuery) {
		nome = jobQuery.getNome();
		descricao = jobQuery.getDescricao();
		query = jobQuery.getSql();
		method = null;
	}

	public static TaskPayloadQuery DML(
		@NonNull String nome,
		@NonNull String descricao,
		@NonNull String query) {

		return new TaskPayloadQuery(
			nome,
			descricao,
			query,
			SqlSintaxe.QueryMethod.DML);
	}

	public static TaskPayloadQuery DQL(
		@NonNull String nome,
		@NonNull String descricao,
		@NonNull String query) {

		return new TaskPayloadQuery(
			nome,
			descricao,
			query,
			SqlSintaxe.QueryMethod.DQL);
	}

	public static TaskPayloadQuery DML(@NonNull PipelineQueryInputDTO dto) {
		return new TaskPayloadQuery(
			dto.getNome().orElse("Anônima"),
			dto.getDescricao().orElse(""),
			dto.getSql(),
			SqlSintaxe.QueryMethod.DML);
	}

	public static TaskPayloadQuery DQL(@NonNull PipelineQueryInputDTO dto) {
		return new TaskPayloadQuery(
			dto.getNome().orElse("Anônima"),
			dto.getDescricao().orElse(""),
			dto.getSql(),
			SqlSintaxe.QueryMethod.DQL);
	}

}
