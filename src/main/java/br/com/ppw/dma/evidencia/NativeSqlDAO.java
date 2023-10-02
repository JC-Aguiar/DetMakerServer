package br.com.ppw.dma.evidencia;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class NativeSqlDAO {

    private static final String SQL_GET_FIELDS_FROM_TABLE =
        "SELECT a.column_name " +
        "FROM all_tab_columns a " +
        "WHERE a.table_name = :tableName ";

    private static final String SQL_VALUES_FROM_TABLE =
        "SELECT :fields " +
        "FROM :tableName " +
        "WHERE :whereQuery ";

    @PersistenceContext
    private EntityManager entityManager;

    public List<String> getFieldsFromTable(@NotBlank String tableName) {
        val session = entityManager.unwrap(Session.class);
        return session.createNativeQuery(SQL_GET_FIELDS_FROM_TABLE)
            .setParameter("tableName", tableName)
            .getResultList()
            .stream()
            .map(String::valueOf)
            .toList();
    }

    public Map<String, Object> getMapFromFieldsTableAndFilter(
        @NotEmpty List<String> fields, @NotBlank String tableName, @NotBlank String whereQuery) {
        //---------------------------------------------------------------------------------------
        //Obtendo valores das diferentes colunas de cada registro no banco dessa determinada tabela
        val session = entityManager.unwrap(Session.class);
        final List<Object[]> queryResult = session.createNativeQuery(SQL_VALUES_FROM_TABLE)
            .setParameter("fields", fields)
            .setParameter("tableName", tableName)
            .setParameter("whereQuery", whereQuery)
            .getResultList();

        //Iterando cada registro (variável 'obj') para extrair seu valor
        val resultSet = new HashMap<String, Object>();
        for(int i = 0; i < fields.size(); i++) {
            for(val obj : queryResult)
                resultSet.put(fields.get(i), obj[i]);
        }
        return resultSet;
    }

}
