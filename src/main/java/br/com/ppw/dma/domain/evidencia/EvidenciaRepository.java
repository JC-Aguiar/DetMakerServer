package br.com.ppw.dma.domain.evidencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {

}
