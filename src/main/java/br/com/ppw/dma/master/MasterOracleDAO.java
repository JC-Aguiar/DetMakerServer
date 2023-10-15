package br.com.ppw.dma.master;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MasterOracleDAO {

    private static final String SQL_GET_FIELDS_FROM_TABLE =
        "SELECT a.column_name " +
        "FROM all_tab_columns a " +
        "WHERE a.table_name = :tableName AND " +
        "ROWNUM <= 125 " +
        "ORDER BY COLUMN_ID ";

    private static final String SQL_VALUES_FROM_TABLE = "SELECT :fields FROM :tableName ";

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

    public List<Map<String, Object>> getFieldAndValuesFromTable(
        @NotEmpty List<String> fields, @NotBlank String tableName, String whereQuery) {
        //---------------------------------------------------------------------------------------
        //Obtendo valores das diferentes colunas de cada registro no banco dessa determinada tabela
        val resultadoFinal = new ArrayList<Map<String, Object>>();
        val session = entityManager.unwrap(Session.class);
//        final List<Object[]> queryResult = session.createNativeQuery(SQL_VALUES_FROM_TABLE)
//            .setParameter("fields", fields)
//            .setParameter("tableName", tableName)
//            .setParameter("whereQuery", whereQuery)
//            .getResultList();
        if(fields.size() == 1) return getFieldAndValuesFromTable(fields.get(0), tableName, whereQuery);

        String sql = SQL_VALUES_FROM_TABLE
            .replace(":fields", String.join(", ", fields))
            .replace(":tableName", tableName);
        if(whereQuery != null) sql = sql + "WHERE " + whereQuery + "ROWNUM <= 50";
        else sql = sql + "WHERE ROWNUM <= 50";

        session.createNativeQuery(sql).getResultList().forEach(obj -> {
            log.debug("{}", obj);
            Object[] elemento = (Object[]) obj;
            val resultSet = new HashMap<String, Object>();
            for(int i = 0; i < fields.size(); i++) {
                resultSet.put(fields.get(i), elemento[i]);
            }
            resultadoFinal.add(resultSet);
        });
        //Exibindo resultado
        log.info("Total de registros coletados da tabela '{}': {}.", tableName, resultadoFinal.size());
        resultadoFinal.forEach(
            map -> map.forEach(
                (k, v) -> log.debug("{}: {}", k, v)
        ));
        return resultadoFinal;
    }

    public List<Map<String, Object>> getFieldAndValuesFromTable(
        @NotBlank String field, @NotBlank String tableName, String whereQuery) {
        //---------------------------------------------------------------------------------------
        //Obtendo valores das diferentes colunas de cada registro no banco dessa determinada tabela
        val resultadoFinal = new ArrayList<Map<String, Object>>();
        val session = entityManager.unwrap(Session.class);

        String sql = SQL_VALUES_FROM_TABLE
            .replace(":fields", field)
            .replace(":tableName", tableName);
        if(whereQuery != null) sql = sql + "WHERE " + whereQuery + "ROWNUM <= 50";
        else sql = sql + "WHERE ROWNUM <= 50";

        session.createNativeQuery(sql).getResultList().forEach(obj -> {
            log.debug("{}", obj);
            String elemento = (String) obj;
            val resultSet = new HashMap<String, Object>();
            resultSet.put(field, elemento);
            resultadoFinal.add(resultSet);
        });
        //Exibindo resultado
        resultadoFinal.forEach(
            map -> map.forEach(
                (k, v) -> log.info("{}: {}", k, v)
            ));
        return resultadoFinal;
    }

}
