package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.cliente.Cliente;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

//    @EntityGraph(attributePaths = { "parametros", "cargas", "logs", "remessas" })
//    @Query("""
//    SELECT DISTINCT     j AS job,
//                        jp AS parametros,
//                        jc AS cargas,
//                        jl AS logs,
//                        jr AS remessas
//    FROM                PPW_JOB j
//    LEFT JOIN FETCH     j.parametros jp
//    LEFT JOIN FETCH     j.cargas jc
//    LEFT JOIN FETCH     j.logs jl
//    LEFT JOIN FETCH     j.remessas jr
//    INNER JOIN          j.cliente c
//    WHERE               c.id    = :clienteId
//    """)
//    List<JobFullProjection> findAllByClienteId(@NonNull Long clienteId);

    @Query(value = "SELECT * FROM PPW_JOB j WHERE j.CLIENTE_ID = :clienteId", nativeQuery = true)
    List<Job> findAllByClienteId(@NonNull Long clienteId);

    @Query(value = "SELECT ID AS id, NOME as nome FROM PPW_JOB WHERE CLIENTE_ID = :clienteId", nativeQuery = true)
    List<JobReferenceProjection> findIdAndNomeByClienteId(@NonNull Long clienteId);

    List<Job> findByClienteAndNomeIn(@NonNull Cliente cliente, @NonNull Collection<String> nomes);

    @Async
    @Query(value = "SELECT * FROM PPW_JOB WHERE ID = :id", nativeQuery = true)
    CompletableFuture<Optional<Job>> asyncOneById(Long id);

    @Async
    @Query(value = "SELECT * FROM PPW_JOB WHERE ID = :ids", nativeQuery = true)
    CompletableFuture<List<Job>> asyncAllById(Collection<Long> ids);

    List<Cliente> findAllClienteByIdIn(@NonNull List<Long> id);

}
