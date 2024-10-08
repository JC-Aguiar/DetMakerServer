package br.com.ppw.dma.domain.massa;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MassaTabelaRepository extends JpaRepository<MassaTabela, Long> {

    @Query(nativeQuery = true, value =
        "SELECT mt.* " +
        "FROM   PPW_MASSA_TABELA mt " +
        "JOIN   PPW_CLIENTE c " +
        "ON     mt.CLIENTE_ID = c.ID " +
        "WHERE  mt.CLIENTE_ID = :clienteId ")
    List<MassaTabela> findAllByClienteId(@NotNull Long clienteId);

    @Query(nativeQuery = true, value =
        "SELECT mt.* " +
        "FROM   PPW_MASSA_TABELA mt " +
        "JOIN   PPW_CLIENTE c " +
        "ON     mt.CLIENTE_ID = c.ID " +
        "WHERE  mt.CLIENTE_ID = :clienteId " +
        "AND    mt.TABELA_NOME IN (:nomes) ")
    List<MassaTabela> findByClienteIdAndNomes(@NotNull Long clienteId, @NotNull Set<String> nomes);

}
