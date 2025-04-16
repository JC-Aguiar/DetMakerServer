package br.com.ppw.dma.domain.task;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Valid
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class TaskPayload {

	@NotBlank String pipelineNome;
	@NotBlank String pipelineDescricao;
	@NotEmpty List<TaskPayloadJob> jobs = new ArrayList<>();
	@Builder.Default List<TaskPayloadQuery> queriesPrePipeline = new ArrayList<>();
	@Builder.Default List<TaskPayloadQuery> queriesPosPipeline = new ArrayList<>();

}
