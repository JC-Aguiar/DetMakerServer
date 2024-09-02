package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.master.QueryMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueuePayloadQuery {

	String nome;
	String query;
	//String descricao;
	QueryMethod method;

}
