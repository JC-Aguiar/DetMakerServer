package br.com.ppw.dma.execFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecFileRepository extends JpaRepository<ExecFile, Long> {

}
