package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.cliente.Cliente;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query(value = "SELECT * FROM PPW_JOB j WHERE j.CLIENTE_ID = :clienteId", nativeQuery = true)
    List<Job> findAllByClienteId(@NonNull Long clienteId);

    List<Job> findByClienteAndNomeIn(@NonNull Cliente cliente, @NonNull List<String> nomes);

    List<Cliente> findAllClienteByIdIn(@NonNull List<Long> id);

}
