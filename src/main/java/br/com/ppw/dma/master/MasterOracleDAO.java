package br.com.ppw.dma.master;

import br.com.ppw.dma.util.ComandoSql;
import br.com.ppw.dma.util.ResultadoSql;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
public class MasterOracleDAO {

    private static final String SQL_GET_FIELDS_FROM_TABLE =
        "SELECT a.column_name " +
        "FROM all_tab_columns a " +
        "WHERE a.table_name = :tableName AND " +
        "ROWNUM <= 125 " +
        "ORDER BY COLUMN_ID ";

    @PersistenceContext
    private EntityManager entityManager;

    public List<String> getFieldsFromTable(@NotBlank String tableName) {
        log.info("Acessando no banco os nomes dos campos da tabela '{}'.", tableName);
        val session = entityManager.unwrap(Session.class);
        val campos = session.createNativeQuery(SQL_GET_FIELDS_FROM_TABLE)
            .setParameter("tableName", tableName)
            .getResultList()
            .stream()
            .map(String::valueOf)
            .toList();
        log.info("Total de campos coletados da tabela '{}': {}.", tableName, campos.size());
        return campos;
    }

    public ResultadoSql getFieldAndValuesFromTable(@NonNull ResultadoSql resultadoSql) {
        if(resultadoSql.semTabela()) throw new RuntimeException("Tabela não definida.");
        resultadoSql.setCampos(checkFields(resultadoSql));
        val fields = resultadoSql.getCampos();
        val tableName = resultadoSql.getTabela();
        val session = entityManager.unwrap(Session.class);
        val sql = resultadoSql.getSqlCompleta();

        //Obtendo valores das colunas de cada registro para a tabela informada
        val extracao = session.createNativeQuery(sql).getResultList();
        log.info("Total de registros na tabela '{}': {}.", tableName, extracao.size());
        extracao.forEach(obj -> {
            log.debug("{}", obj);
            Object[] elemento = (Object[]) obj;
            val resultSet = new HashMap<String, Object>();

            for(int i = 0; i < fields.size(); i++) {
                resultSet.put(fields.get(i), elemento[i]);
            }
            resultSet.forEach((k, v) -> log.debug(" - Coletado: '{}' = {}", k, v));
            resultadoSql.addResultado(resultSet);
        });
        return resultadoSql.fecharConsultaPreJob();
    }

    public List<String> checkFields(@NonNull ComandoSql sql) {
        if(sql.semCampos()) return getFieldsFromTable(sql.getTabela());
        return sql.getCampos();
    }

}
