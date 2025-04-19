package br.com.ppw.dma.domain.ambiente;

import br.com.ppw.dma.domain.cliente.Cliente;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmbienteRepository extends JpaRepository<Ambiente, Long> {

    Ambiente findAllByNome(String nome);

    List<Ambiente> findAllByCliente(@NotNull Cliente cliente);

    List<Ambiente> findAllByClienteIn(@NotNull List<Cliente> cliente);

    boolean existsByNome(String nome);

}
