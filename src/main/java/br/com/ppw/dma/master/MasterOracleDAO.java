package br.com.ppw.dma.master;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppware.api.MassaPreparada;
import br.com.ppware.api.TipoColuna;
import jakarta.persistence.PersistenceException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.exception.SQLGrammarException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class MasterOracleDAO implements AutoCloseable {

    final String url;
    final String username;
    final String password;
    final Connection conn;


    public MasterOracleDAO(@NonNull Ambiente ambiente) throws SQLException {
        this(ambiente.acessoBanco());
    }

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

    public List<DbTable> extractInfoFromTables(@NonNull String tabela) {
        return extractInfoFromTables(Set.of(tabela), Set.of());
    }

    public List<DbTable> extractInfoFromTables(@NonNull Set<String> tabelas) {
        return extractInfoFromTables(tabelas, Set.of());
    }

    public List<DbTable> extractInfoFromTables(@NonNull QueryExtraction extraction) {
        var tables = extraction.tables();
        var columns = extraction.columns();
        extraction.filters()
            .parallelStream()
            .map(QueryFilter::column)
            .forEach(columns::add);

        log.info("Iniciando coleta de metadados.");
        var dbInfo = extractInfoFromTables(tables, columns);

        log.info("Vinculando variáveis das queries no resultado do banco.");
        extraction.filters().parallelStream().forEach(
            queryFilter -> dbInfo
                .parallelStream()
                .map(DbTable::colunas)
                .flatMap(Set::parallelStream)
                .forEach(dbCol -> dbCol.addVariable(queryFilter))
        );
        return dbInfo;
    }

    public List<DbTable> extractInfoFromTables(
        @NonNull Set<String> tabelas,
        @NonNull Set<String> colunas) {

        if(tabelas.isEmpty()) return List.of();

        var tabelasConsultadas = new ArrayList<DbTable>();
        var sql = queryForMetadates(tabelas, colunas);
        try(val statement = conn.prepareStatement(sql);
            var resultado = statement.executeQuery()) {

            log.info("Query executada com sucesso. Coletando dados das tabelas.");
            while(resultado.next()) {
                var nomeTabela = resultado.getString("TABLE_NAME");
                var metadata = DbColumnMetadata.builder()
                    .type(TipoColuna.from(resultado.getString("DATA_TYPE")))
                    .length(resultado.getInt("DATA_LENGTH"))
                    .precision(resultado.getInt("DATA_PRECISION"))
                    .scale(resultado.getInt("DATA_SCALE"))
                    .build();
                var coluna = DbColumn.builder()
                    .name(resultado.getString("COLUMN_NAME"))
                    .metadata(metadata)
                    .variables(new HashSet<>())
                    .build();

                tabelasConsultadas.parallelStream()
                    .filter(t -> t.tabela().equalsIgnoreCase(nomeTabela))
                    .findFirst()
                    .ifPresentOrElse(
                        t -> t.colunas().add(coluna),
                        () -> tabelasConsultadas.add(
                            new DbTable(nomeTabela, null, new HashSet<>(Set.of(coluna)))
                    ));
            }
            log.info("Total de registros coletados: {}", tabelasConsultadas.size());
            return tabelasConsultadas;
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
        }
    }

    private static String queryForMetadates(@NonNull Set<String> tabelas) {
        return queryForMetadates(tabelas, Set.of());
    }

    private static String queryForMetadates(
        @NonNull Set<String> tabelas,
        @NonNull Set<String> colunas) {

        var tabelasNomes = tabelas.parallelStream()
            .map(nome -> "'" +nome+ "'")
            .collect(Collectors.joining(", "));

        var colunasNomes = colunas.parallelStream()
            .map(nome -> "'" +nome+ "'")
            .collect(Collectors.joining(", "));

        var sqlAppend = colunas.isEmpty()
            ? ""
            : "AND COLUMN_NAME IN (" +colunasNomes+ ") ";

        var sql = "SELECT TABLE_NAME, COLUMN_NAME, DATA_LENGTH, DATA_TYPE, DATA_PRECISION, DATA_SCALE "
                + "FROM ALL_TAB_COLUMNS "
                + "WHERE TABLE_NAME IN (" +tabelasNomes+ ") "
                + sqlAppend
                + "ORDER BY TABLE_NAME ";
        log.info("SQL: {}", sql);
        return sql;
    }

    //TODO: javadoc (explicar que tem um throw RuntimeException ou talvez criar um throw próprio para tal)
    public List<Map<String, Object>> collectData(@NonNull String sql)
    throws SQLException {
        checkSqlGrammar(sql);
        val extracao = new ArrayList<Map<String, Object>>();
        try(val statement = conn.createStatement()) {
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
            log.info("Nenhum registro encontrado");
        else
            log.info("Total de registros coletados: {}.", extracao.size());
        return extracao;
    }

    //TODO: javadoc (explicar que tem um throw RuntimeException ou talvez criar um throw próprio para tal)
    public void validadeQuery(@NonNull String sql) throws SQLGrammarException, SQLException {
        checkSqlGrammar(sql);
        log.info("SQL: {}", sql);
        log.info("Testando executar query (ROWNUM = 1).");
        try(val statement = conn.createStatement()) {
            statement.setMaxRows(1);
            statement.executeQuery(sql);
            log.info("Query aprovada.");
        }
    }
    
    //TODO: javadoc
//    public void createValidateQuery(@NonNull String sql, @NonNull List<ConfigQueryVar> queryVars)
//    throws SQLGrammarException, SQLException {
//        sql = FormatString.substituirVariaveis(sql, "?");
//        checkSqlGrammar(sql);
//        try(val statement = conn.prepareStatement(sql)) {
//            log.info("Inserindo registros aleatórios e validando tabelas e column da query.");
//            for(var queryVar : queryVars) {
//                var index = queryVar.getIndex();
//                //TODO: gerar valor aleatório STRING
//                var valor = queryVar.gerarValorAleatorio();
//                var array = queryVar.getArray();
//                if(array) {
//                    var type = queryVar.getTipo();
//                    var valorArray = conn.createArrayOf(type.name(), new Object[] {valor, valor});
//                    statement.setArray(index, valorArray);
//                    continue;
//                }
//                switch(valor) {
//                    case(BigDecimal number) -> statement.setBigDecimal(index, number);
//                    case(Float number) -> statement.setFloat(index, number);
//                    case(String string) -> statement.setString(index, string);
//                    case(OffsetDateTime date) -> statement.setTimestamp(
//                        index, Timestamp.from(date.toInstant())
//                    );
//                    default -> throw new RuntimeException("Sem foi possível gerar massa"); //TODO: usar mesma exceção própria do TipoColuna
//                }
//            };
//            log.info("Testando executar query (ROWNUM = 1).");
//            statement.setMaxRows(1);
//            statement.executeQuery(sql);
//            log.info("Query aprovada.");
//        }
//    }

    //TODO: criar exception própria?
    //TODO: javadoc
    private void checkSqlGrammar(@NonNull String sql) {
        log.info("Validando comandos inválidos na query.");
        if(!SqlSintaxe.isSafeSelect(sql))
            throw new RuntimeException("A queries informada contêm comandos DDL/DML não permitidos.");
        log.info("Query válida para uso.");
    }

    //TODO: criar exception própria?
    //TODO: javadoc
    //TODO: @throws SQLException
    @SneakyThrows
    public boolean insertSql(@NonNull MassaPreparada massa)  {
        log.info("Validando comandos inválidos para query de insert.");
        val sql = massa.gerarQueryInsert();
        if(!SqlSintaxe.isSafeInsert(sql))
            throw new RuntimeException("A queries informada contêm comandos DDL não permitidos.");
        log.info("Query aprovada.");

        try(val statement = conn.prepareStatement(sql)) {
            massa.preencherColunas(statement);
            log.info("SQL: {}", sql);
            log.info("Executando query.");
            log.info("Registros inseridos com sucesso: {}", statement.executeUpdate());
            return true;
        }
    }

    //TODO: criar exception própria?
    //TODO: javadoc
    //TODO: @throws SQLException
    @SneakyThrows
    public boolean deleteSql(@NonNull MassaPreparada massa)  {
        log.info("Validando comandos inválidos para query de delete.");
        val sql = massa.gerarQueryDelete();
        if(!SqlSintaxe.isSafeDelete(sql))
            throw new RuntimeException("A queries informada contêm comandos DDL não permitidos.");
        log.info("Query aprovada.");

        try(val statement = conn.prepareStatement(sql)) {
            massa.preencherColunas(statement);
            log.info("SQL: {}", sql);
            log.info("Executando query.");
            log.info("Registros inseridos com sucesso: {}", statement.executeUpdate());
            return true;
        }
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
