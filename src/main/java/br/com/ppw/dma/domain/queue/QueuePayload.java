package br.com.ppw.dma.domain.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueuePayload {

	String pipelineNome;
	String pipelineDescricao;
	List<QueuePayloadJob> jobs = new ArrayList<>();
	List<QueuePayloadQuery> massas = new ArrayList<>();

}
