package br.com.ppw.dma.domain.queue;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class QueuePushResponseDTO {

	long ambienteId;
	long queueSize;
	String ticket;


	public QueuePushResponseDTO(long ambienteId, long queueSize) {
		this.ambienteId = ambienteId;
		this.queueSize = queueSize;
		this.ticket = UUID.randomUUID().toString();
	}

	public String getMessage() {
		if(queueSize == 0) return "Sua solicitação está sendo executada agora";
		return "Existem " + queueSize + " solicitações que deve esperar serem finalizadas.";
	}
}
