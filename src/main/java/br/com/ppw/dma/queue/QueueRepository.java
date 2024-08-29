package br.com.ppw.dma.queue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {

}
