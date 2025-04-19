package br.com.ppw.dma.domain.cliente;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByNomeContaining(String nome);

    boolean existsByNome(String nome);


    @Query(nativeQuery = true, value =
        "SELECT c.* FROM PPW_CLIENTE c " +
        "JOIN PPW_AMBIENTE a " +
        "ON a.CLIENTE_ID = c.ID " +
        "WHERE a.ID = :id ")
    Cliente findByAmbienteId(Long id);

}
