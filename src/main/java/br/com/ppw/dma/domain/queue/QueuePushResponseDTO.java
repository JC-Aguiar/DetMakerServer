package br.com.ppw.dma.domain.queue;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString
@FieldDefaults(level = PRIVATE)
public class QueuePushResponseDTO {

	final String ticket;
	final long queueSize;
	final String message;


	public QueuePushResponseDTO(String ticket, long queueSize) {
		this.ticket = ticket;
		this.queueSize = queueSize;
		if(queueSize == 0)
			message = "Sua solicitação está sendo executada agora";
		else
			message = "Existem " + queueSize + " solicitações que deve esperar serem finalizadas.";
	}

	private QueuePushResponseDTO(long queueSize) {
		this.ticket = null;
		this.queueSize = queueSize;
		this.message = "Existem " + queueSize + " solicitações que deve esperar serem finalizadas.";

	}

	public static QueuePushResponseDTO blocked(long queueSize) {
		return new QueuePushResponseDTO(queueSize);
	}
}
