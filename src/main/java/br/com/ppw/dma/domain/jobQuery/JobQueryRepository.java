package br.com.ppw.dma.domain.jobQuery;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobQueryRepository extends JpaRepository<JobQuery, Long> {

    @Query(value = "SELECT cq.* FROM PPW_JOB_QUERY cq WHERE cq.JOB_ID = ?1", nativeQuery = true)
    List<JobQuery> findAllByJobId(Long jobId);

    @Query(nativeQuery = true, value =
        "SELECT cq.* FROM PPW_JOB_QUERY cq " +
        "JOIN PPW_JOB pj ON pj.ID = cq.JOB_ID " +
        "WHERE pj.CLIENTE_ID = :clienteId")
    List<JobQuery> findAllByClienteId(@NotNull Long clienteId);

//    @Query(value = "SELECT * FROM PPW_JOB_QUERY_VAR WHERE JOB_QUERY_ID = :id", nativeQuery = true)
//    List<ConfigQueryVar> findAllVarsByQueryId(@NotNull Long id);

//    @Modifying
//    @Query(value = "DELETE FROM PPW_JOB_QUERY WHERE ID = :id", nativeQuery = true)
//    void deleteQueryById(Long id);

}
