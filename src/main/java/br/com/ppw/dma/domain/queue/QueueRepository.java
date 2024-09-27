package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QueueRepository extends JpaRepository<TaskQueue, Long> {

	@Query(nativeQuery = true, value =
		"SELECT COUNT(1) FROM PPW_QUEUE " +
		"WHERE AMBIENTE_ID = :ambiente AND STATUS = :status")
	long countByStatusInAmbiente(Ambiente ambiente, String status);

	@Query(nativeQuery = true, value =
		"SELECT COUNT(1) FROM PPW_QUEUE WHERE AMBIENTE_ID = :ambienteId")
	long countInAmbiente(Long ambienteId);

//SELECT q FROM TaskQueue q WHERE q.ticket = :ticket JOIN FETCH q.ambiente
//	@EntityGraph(attributePaths = { "ambiente",  })
	@Query(nativeQuery = true, value =
		"SELECT q.*, a.*, c.* FROM PPW_QUEUE q " +
		"JOIN PPW_AMBIENTE a ON q.AMBIENTE_ID = a.ID " +
		"JOIN PPW_CLIENTE c ON a.CLIENTE_ID = c.ID " +
		"WHERE q.TICKET = :ticket")
	Optional<QueueRunnerView> findByTicket(String ticket);

}
