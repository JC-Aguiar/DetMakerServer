package br.com.ppw.dma.security.audition;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Query(nativeQuery = true, value = """
    SELECT  MIN(DATA) AT TIME ZONE 'UTC' 
    FROM    PPW_HISTORICO 
    WHERE   USUARIO = :usuario
    """)
    Optional<OffsetDateTime> findMinDataByUsuario(String usuario);

}
