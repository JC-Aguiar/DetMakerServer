package br.com.ppw.dma.configQuery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigQueryRepository extends JpaRepository<ConfigQuery, Long> {

    @Query(value = "SELECT cq.* FROM PPW_CONFIG_QUERY cq WHERE cq.JOB_ID = ?1", nativeQuery = true)
    List<ConfigQuery> findAllByJobId(Long jobId);

}
