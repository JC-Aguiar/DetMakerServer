package br.com.ppw.dma.domain.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobResourceRepository extends JpaRepository<JobResource, Long> {

    @Query(nativeQuery = true, value = """
        SELECT  *
        FROM    PPW_JOB_RESOURCE
        WHERE   MASCARA = :mascara
        AND     JOB_ID  = :jobId
        """)
    List<JobResource> findAllByMascaraAndJobId(String mascara, Long jobId);

}
