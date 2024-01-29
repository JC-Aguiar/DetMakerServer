package br.com.ppw.dma.cliente;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Cliente findAllByNome(@NotNull String nome);

    boolean existsByNome(@NotNull String nome);

}
