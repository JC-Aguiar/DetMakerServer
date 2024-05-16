package br.com.ppw.dma.massa;

import br.com.ppw.dma.configQuery.ConfigQuery;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MassaTabelaRepository extends JpaRepository<MassaTabela, Long> {

    @Query(nativeQuery = true, value =
        "SELECT mt.* FROM PPW_MASSA_TABELA mt " +
        "JOIN PPW_CLIENTE c ON mt.CLIENTE_ID = c.ID " +
        "WHERE mt.CLIENTE_ID = :clienteId")
    List<MassaTabela> findAllByClienteId(@NotNull Long clienteId);

}
