package br.com.ppw.dma.pipeline;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, PipelineProps> {

    @Query(value = "SELECT * FROM PPW_PIPELINE p WHERE p.NOME = :nome AND p.CLIENTE_ID = :clienteId",
        nativeQuery = true)
    Pipeline findByNomeAndCliente(@NotNull String nome, @NotNull Long clienteId);

    @Query(nativeQuery = true,
        value = "SELECT * FROM PPW_PIPELINE p WHERE p.CLIENTE_ID = :clienteId AND p.OCULTAR < 1")
    List<Pipeline> findAllByClienteId(@NotNull Long clienteId);

//    Ambiente findAllByNome(@NotNull String name);
    boolean existsByNome(@NotNull String nome);

}
