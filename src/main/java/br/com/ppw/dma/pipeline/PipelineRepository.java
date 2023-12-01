package br.com.ppw.dma.pipeline;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    Pipeline findAllByNome(@NotNull String nome);

//    Pipeline findAllByNome(@NotNull String nome);
    boolean existsByNome(@NotNull String nome);

}
