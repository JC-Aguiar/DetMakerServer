package br.com.ppw.dma.domain.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobResourceRepository extends JpaRepository<JobResource, Long> {

}
