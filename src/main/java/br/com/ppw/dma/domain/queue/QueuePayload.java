package br.com.ppw.dma.domain.queue;

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
public class QueuePayload {

	@NotBlank String pipelineNome;
	@NotBlank String pipelineDescricao;
	@NotEmpty List<QueuePayloadJob> jobs = new ArrayList<>();
	@Builder.Default List<QueuePayloadQuery> queriesPrePipeline = new ArrayList<>();
	@Builder.Default List<QueuePayloadQuery> queriesPosPipeline = new ArrayList<>();

}
