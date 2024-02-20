package br.com.ppw.dma.configQuery;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigQueryRepository extends JpaRepository<ConfigQuery, Long> {

    @Query(value = "SELECT cq.* FROM PPW_CONFIG_QUERY cq WHERE cq.JOB_ID = ?1", nativeQuery = true)
    List<ConfigQuery> findAllByJobId(Long jobId);

    @Query(nativeQuery = true, value =
        "SELECT cq.* FROM PPW_CONFIG_QUERY cq " +
        "JOIN PPW_JOB pj ON pj.ID = cq.JOB_ID " +
        "WHERE pj.CLIENTE_ID = :clienteId")
    List<ConfigQuery> findAllByClienteId(@NotNull Long clienteId);

}
