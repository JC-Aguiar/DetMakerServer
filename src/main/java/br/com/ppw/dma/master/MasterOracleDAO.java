package br.com.ppw.dma.master;

import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ResultadoSql;
import br.com.ppw.dma.util.ValidadorSQL;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.Session;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    //TODO: javadoc (explicar que tem um throw RuntimeException ou talvez criar um throw próprio para tal)
    public ResultadoSql getAllInfoFromTable(@NonNull ResultadoSql resultadoSql) {
        //Validações
        if(resultadoSql.semTabela()) throw new RuntimeException("Tabela não definida.");
        validateInputs(resultadoSql);

        //Preparando execução
        val tableName = resultadoSql.getTabela();
        val session = entityManager.unwrap(Session.class);
        val sql = resultadoSql.getSqlCompleta();
        val query = session.createNativeQuery(sql);

        //Configurando tipo de retorno para uma lista de mapas
        query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
        final List<Map<String, Object>> extracao = query.getResultList();
        if(extracao.isEmpty()) {
            log.info("Nenhum registro obtido na tabela '{}'",  tableName);
            return resultadoSql;
        }
        log.info("Total de registros na tabela '{}': {}.", tableName, extracao.size());

        //Adicionando os campos do primeiro registro para o ResultadoSql dessa mesma tabela
        if(resultadoSql.getCampos().isEmpty()) {
            extracao.get(0)
                .keySet()
                .forEach(resultadoSql.getCampos()::add);
            log.debug(" - Campos: {}", String.join(", ", resultadoSql.getCampos()));
        }
        //Adicionando os valores de cada registro para o ResultadoSql dessa mesma tabela
        extracao.forEach(obj -> {
            obj.values().forEach(v -> log.debug(" - Valores: {}", v));
            resultadoSql.addResultado(obj);
        });
        return resultadoSql.fecharConsultaPreJob();
    }

    public List<String> checkFields(@NonNull ComandoSql sql) {
        if(sql.getCampos().isEmpty()) return getFieldsFromTable(sql.getTabela());
        return sql.getCampos();
    }

    //TODO: criar exception própria?
    //TODO: javadoc
    public void validateInputs(@NonNull ComandoSql sql) {
        log.info("Validando valores preenchidos nos filtros e no Sql base.");
        val filtros = sql.getFiltros()
            .stream()
            .map(String::valueOf)
            .toList();

//        boolean camposValidos = ValidadorSQL.isSafe(sql.getCampos());
        boolean filtroValido = ValidadorSQL.isSafe(filtros);
        boolean sqlValido = ValidadorSQL.isSafe(sql.getSql());
        if(!filtroValido || !sqlValido) {
            throw new RuntimeException("A queries informada contêm comandos DDL não permitidos.");
        }
    }

    //TODO: remover
    public void teste() {
        val session = entityManager.unwrap(Session.class);
        val query = session.createNativeQuery("SELECT * FROM EVENTOS_WEB WHERE ROWNUM <= 50");
        query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);
        List<Map<String, Object>> results = query.getResultList();
        if (!results.isEmpty()) {
            Map<String, Object> firstRow = results.get(1);
            Set<String> fieldNames = firstRow.keySet();
            for (String fieldName : fieldNames) {
                System.out.println("Campo consultado: " + fieldName);
            }
        }
    }

}
