package br.com.ppw.dma.domain.queue;

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
public class QueuePayloadQuery {

	@NotBlank String nome;
	@NotBlank String descricao;
	@NotBlank String query;
	@Nullable SqlSintaxe.QueryMethod method; //TODO: remover?


	public QueuePayloadQuery(@NonNull ExecQuery execQuery) {
		nome = execQuery.getQueryNome();
		descricao = execQuery.getQueryDescricao();
		query = execQuery.getQuery();
		method = null;
	}

	public QueuePayloadQuery(@NonNull JobQuery jobQuery) {
		nome = jobQuery.getNome();
		descricao = jobQuery.getDescricao();
		query = jobQuery.getSql();
		method = null;
	}

	public static QueuePayloadQuery DML(
		@NonNull String nome,
		@NonNull String descricao,
		@NonNull String query) {

		return new QueuePayloadQuery(
			nome,
			descricao,
			query,
			SqlSintaxe.QueryMethod.DML);
	}

	public static QueuePayloadQuery DQL(
		@NonNull String nome,
		@NonNull String descricao,
		@NonNull String query) {

		return new QueuePayloadQuery(
			nome,
			descricao,
			query,
			SqlSintaxe.QueryMethod.DQL);
	}

	public static QueuePayloadQuery DML(@NonNull PipelineQueryInputDTO dto) {
		return new QueuePayloadQuery(
			dto.getNome().orElse("Anônima"),
			dto.getDescricao().orElse(""),
			dto.getSql(),
			SqlSintaxe.QueryMethod.DML);
	}

	public static QueuePayloadQuery DQL(@NonNull PipelineQueryInputDTO dto) {
		return new QueuePayloadQuery(
			dto.getNome().orElse("Anônima"),
			dto.getDescricao().orElse(""),
			dto.getSql(),
			SqlSintaxe.QueryMethod.DQL);
	}

}
