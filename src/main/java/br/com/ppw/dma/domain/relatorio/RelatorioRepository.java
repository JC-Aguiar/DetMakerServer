package br.com.ppw.dma.domain.relatorio;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    List<Relatorio> findAllByPipelineNome(@NonNull String pipelineNome);

    @Query(value = "SELECT * FROM PPW_RELATORIO p WHERE p.AMBIENTE_ID = :ambienteId", nativeQuery = true)
    List<Relatorio> findAllByAmbienteId(@NonNull Long ambienteId);

    @Query(value = "SELECT * FROM PPW_RELATORIO p WHERE p.AMBIENTE_ID = :ambienteId", nativeQuery = true)
    Page<Relatorio> findAllByAmbienteId(@NonNull Long ambienteId, @NonNull Pageable pageable);

}
