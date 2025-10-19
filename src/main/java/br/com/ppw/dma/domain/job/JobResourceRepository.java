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
public interface JobResourceRepository extends JpaRepository<JobResource, Long> {

    @Async
    @Query(value = "SELECT * FROM PPW_JOB_RESOURCE WHERE JOB_ID = :jobIds", nativeQuery = true)
    CompletableFuture<List<JobResource>> asyncAllByJobId(Collection<Long> jobIds);

    default CompletableFuture<List<JobResource>> asyncAllByJobId(Long...jobIds) {
        return asyncAllByJobId(Set.of(jobIds));
    }

}
