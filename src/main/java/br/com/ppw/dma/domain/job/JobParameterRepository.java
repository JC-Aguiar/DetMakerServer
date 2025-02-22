package br.com.ppw.dma.domain.job;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobParameterRepository extends JpaRepository<JobParameter, Long> {

    @Query(nativeQuery = true, value = """
        SELECT  *
        FROM    PPW_JOB_PARAMETER
        WHERE   NOME   = :nome
        AND     JOB_ID = :jobId
        """)
    List<JobParameter> findAllByNomeAndJobId(String nome, Long jobId);

}
