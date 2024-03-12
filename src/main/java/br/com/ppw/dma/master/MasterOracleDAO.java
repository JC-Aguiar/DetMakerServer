package br.com.ppw.dma.master;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ResultadoSql;
import br.com.ppw.dma.util.SqlUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.exception.SQLGrammarException;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class MasterOracleDAO implements AutoCloseable {

    final String url;
    final String username;
    final String password;
    final Connection conn;

    public MasterOracleDAO(@NonNull AmbienteAcessoDTO acessoBanco) throws SQLException {
        url = "jdbc:oracle:thin:@" + acessoBanco.getConexao();
        username = acessoBanco.getUsuario();
        password = acessoBanco.getSenha();
        log.info("Preparando conexão de banco:");
        log.info(" - URL: '{}'", url);
        log.info(" - USER: '{}'", username);
        conn = DriverManager.getConnection(url, username, password);
        log.info("Conexão realizada com sucesso.");
    }

    public List<String> getColsFromTable(@NotBlank String tableName)
    throws SQLException {
        log.info("Obtendo os nomes dos campos para a tabela '{}'.", tableName);
        if(!SqlUtils.isSafeQuery(tableName)) {
            throw new RuntimeException("A tabela informada é/contêm comandos DDL não permitidos.");
        }
        val listaColunas = new ArrayList<String>();
        try(val statement = conn.createStatement()) {
            val sql = sqlGetColsFromTable(tableName);
            log.info("SQL: {}", sql);
            log.info("Executando query.");
            val resultSet = statement.executeQuery(sql);
            val metaDados = resultSet.getMetaData();
            int columnCount = metaDados.getColumnCount();
            while(resultSet.next()) {
                for(int i = 1; i <= columnCount; i++) {
                    listaColunas.add(resultSet.getString(i));
                }
            }
            log.info("Total de campos coletados da tabela '{}': {}.", tableName, listaColunas.size());
            return listaColunas;
        }
    }

    private static String sqlGetColsFromTable(@NotBlank String tableName) {
        String sql = "SELECT a.column_name " +
                "FROM all_tab_columns a " +
                "WHERE a.table_name = '%s' " +
                "AND ROWNUM <= 125 " +
                "ORDER BY COLUMN_ID ";
        return String.format(sql, tableName);
    }

    //TODO: javadoc (explicar que tem um throw RuntimeException ou talvez criar um throw próprio para tal)
    public void validateQuery(@NonNull String sql) throws SQLGrammarException, SQLException {
        log.info("SQL: {}", sql);
        validateSql(sql);
        log.info("Validando tabelas e colunas da query.");
        try(val statement = conn.createStatement()) {
            statement.setMaxRows(1);
            statement.executeQuery(sql);
        }
        log.info("Query aprovada.");
    }

    //TODO: javadoc (explicar que tem um throw RuntimeException ou talvez criar um throw próprio para tal)
    public ResultadoSql getAllInfoFromTable(@NonNull ResultadoSql resultadoSql) throws SQLException {
//        if(resultadoSql.semTabela()) throw new RuntimeException("Tabela não definida.");
        log.info("Executando query '{}'.", resultadoSql.getTabela());
        val tableName = resultadoSql.getTabela();
        val extracao = new ArrayList<Map<String, Object>>();

        try(val statement = conn.createStatement()) {
            val sql = resultadoSql.getSqlCompleta();
            log.info("SQL: {}", sql);
            validateSql(sql);
            log.info("Executando query.");
            val resultSet = statement.executeQuery(sql);
            val metaDados = resultSet.getMetaData();
            int columnCount = metaDados.getColumnCount();

            while(resultSet.next()) {
                val resultMap = new HashMap<String, Object>();

                for(int i = 1; i <= columnCount; i++) {
                    val coluna = metaDados.getColumnName(i);
                    val valor = resultSet.getObject(i);
                    resultMap.put(coluna, valor);
                }
                extracao.add(resultMap);
            }
        }
        if(extracao.isEmpty())
            log.info("Nenhum registro para a query '{}'.",  tableName);
        else
            log.info("Total de registros para a query '{}': {}.", tableName, extracao.size());

        //Adicionando os campos do primeiro registro para o ResultadoSql dessa mesma tabela
        if(resultadoSql.getCampos().isEmpty()) {
            extracao.get(0)
                .keySet()
                .forEach(resultadoSql.getCampos()::add);
            log.debug(" - Campos: {}", String.join(", ", resultadoSql.getCampos()));
        }
        //Adicionando os valores de cada registro para o ResultadoSql dessa mesma tabela
        extracao.forEach(resultadoSql::addResultado);
        return resultadoSql;
    }

    public List<String> checkFields(@NonNull ComandoSql sql) throws SQLException {
        if(sql.getCampos().isEmpty()) return getColsFromTable(sql.getTabela());
        return sql.getCampos();
    }

    //TODO: criar exception própria?
    //TODO: javadoc
    public void validateSql(@NonNull String sql) {
        log.info("Validando comandos inválidos na query.");
        if(!SqlUtils.isSafeQuery(sql)) {
            throw new RuntimeException("A queries informada contêm comandos DDL não permitidos.");
        }
        log.info("Query aprovada.");
    }

    @Override
    public void close() {
        try {
            if(conn != null) conn.close();
        }
        catch(SQLException e) {
            log.error("Erro inesperado ao tentar fechar conexão: {}", e.getMessage());
        }
    }
}
