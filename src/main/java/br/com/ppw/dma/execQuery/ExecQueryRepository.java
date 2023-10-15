package br.com.ppw.dma.execQuery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecQueryRepository extends JpaRepository<ExecQuery, Long> {

}
