//package br.com.ppw.dma.domain.task;
//
//import br.com.ppw.dma.domain.ambiente.Ambiente;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface TaskRepository extends JpaRepository<RemoteTask, Long> {
//
//	@Query(nativeQuery = true, value = """
//	SELECT 	COUNT(1)
//	FROM 	PPW_QUEUE
//	WHERE 	AMBIENTE_ID = :ambiente
//	AND 	STATUS 		= :status
//	""")
//	long countByStatusInAmbiente(Ambiente ambiente, String status);
//
//	default long countByStatusInAmbiente(Ambiente ambiente, TaskStatus status) {
//		return countByStatusInAmbiente(ambiente, status.name());
//	}
//
//	@Query(nativeQuery = true, value = """
//	SELECT 	COUNT(1)
//	FROM 	PPW_QUEUE
//	WHERE 	AMBIENTE_ID = :ambienteId
//	""")
//	long countByAmbiente(Long ambienteId);
//
//	@Query(nativeQuery = true, value = """
//	SELECT 		*
//	FROM 		PPW_QUEUE
//	WHERE 		AMBIENTE_ID = :ambienteId
//	ORDER BY 	DATA_SOLICITACAO DESC
//	""")
//	List<RemoteTask> findAllInAmbiente(Long ambienteId);
//
//	@Query(nativeQuery = true, value = """
//	SELECT 	*
//	FROM 	(
//			SELECT 		*
//			FROM 		PPW_QUEUE
//			WHERE 		AMBIENTE_ID = :ambienteId
//			AND         STATUS      = :status
//			ORDER BY 	DATA_SOLICITACAO ASC
//			)
//	WHERE 	ROWNUM = 1
//	""")
//	Optional<RemoteTask> findLastTaskByStatusInAmbiente(Long ambienteId, String status);
//
//	default Optional<RemoteTask> findLastTaskByStatusInAmbiente(Long ambienteId, TaskStatus status) {
//		return findLastTaskByStatusInAmbiente(ambienteId, status.name());
//	}
//
//	@Query(value ="SELECT COUNT(1) FROM PPW_QUEUE WHERE AMBIENTE_ID = :ambienteId", nativeQuery = true)
//	long countInAmbiente(Long ambienteId);
//
//	@Modifying
//	@Query(name = "DELETE FROM PPW_QUEUE q WHERE q.TICKET = :ticket", nativeQuery = true)
//	boolean deleteByTicket(String ticket);
//
//}
