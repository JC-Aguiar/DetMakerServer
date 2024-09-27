package br.com.ppw.dma.domain.cliente;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Cliente findAllByNome(@NotNull String nome);

    boolean existsByNome(@NotNull String nome);


    @Query(nativeQuery = true, value =
        "SELECT c.* FROM PPW_CLIENTE c " +
        "JOIN PPW_AMBIENTE a " +
        "ON a.CLIENTE_ID = c.ID " +
        "WHERE a.ID = :id ")
    Cliente findByAmbienteId(Long id);

}
