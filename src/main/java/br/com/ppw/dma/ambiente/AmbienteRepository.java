package br.com.ppw.dma.ambiente;

import br.com.ppw.dma.cliente.Cliente;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbienteRepository extends JpaRepository<Ambiente, Long> {

    Ambiente findAllByNome(@NotNull String nome);

    List<Ambiente> findAllByCliente(@NotNull Cliente cliente);

    boolean existsByNome(@NotNull String nome);

}
