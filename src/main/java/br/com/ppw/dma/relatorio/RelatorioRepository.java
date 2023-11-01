package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.pipeline.Pipeline;
import br.com.ppw.dma.relatorio.Relatorio;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    List<Relatorio> findAllByPipeline(@NonNull Pipeline pipeline);

}
