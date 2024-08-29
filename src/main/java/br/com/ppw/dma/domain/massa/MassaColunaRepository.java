package br.com.ppw.dma.domain.massa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MassaColunaRepository extends JpaRepository<MassaColuna, Long> {

}
