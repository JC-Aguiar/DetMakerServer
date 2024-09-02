package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {

	@Query(nativeQuery = true, value =
		"SELECT COUNT(1) FROM PPW_QUEUE " +
		"WHERE AMBIENTE_ID = :ambiente AND STATUS = :status")
	long countByStatusInAmbiente(Ambiente ambiente, String status);

	@Query(nativeQuery = true, value =
		"SELECT COUNT(1) FROM PPW_QUEUE WHERE AMBIENTE_ID = :ambiente")
	long countInAmbiente(Ambiente ambiente);
}
