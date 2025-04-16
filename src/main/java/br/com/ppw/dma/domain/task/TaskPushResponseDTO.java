package br.com.ppw.dma.domain.task;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@ToString
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class TaskPushResponseDTO {

	long ambienteId;
	long queueSize;
	String ticket;
	TaskStatus status;


	public String getMessage() {
		if(queueSize == 0)
			return "Sua solicitação estará sendo executada agora.";
		return "Sua solicitação será executada após a finalização de %d tarefa(s).".formatted(queueSize);
	}

}
