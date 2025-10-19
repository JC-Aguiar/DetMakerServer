package br.com.ppw.dma.domain.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Repository
public interface JobParameterRepository extends JpaRepository<JobParameter, Long> {

    @Async
    @Query(value = "SELECT * FROM PPW_JOB_PARAMETER WHERE JOB_ID = :jobIds", nativeQuery = true)
    CompletableFuture<List<JobParameter>> asyncAllByJobId(Collection<Long> jobIds);

    default CompletableFuture<List<JobParameter>> asyncAllByJobId(Long...jobIds) {
        return asyncAllByJobId(Set.of(jobIds));
    }

}
