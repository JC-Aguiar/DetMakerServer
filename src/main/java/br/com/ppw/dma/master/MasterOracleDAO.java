package br.com.ppw.dma.master;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.configQuery.ColumnInfo;
import br.com.ppw.dma.configQuery.FiltroSql;
import br.com.ppw.dma.util.SqlUtils;
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

    public Map<String, ColumnInfo> getColumnInfo(
        @NonNull String tabela,
        @NonNull Set<String> colunas) {
        //----------------------------------------
        var mapaColunas = new HashMap<String, ColumnInfo>();
        var colunasString = colunas.parallelStream()
            .map(col -> "'" +col+ "'")
            .collect(Collectors.joining(", "));

        var sql = "SELECT COLUMN_NAME, DATA_LENGTH, DATA_TYPE, DATA_PRECISION, DATA_SCALE "
            + "FROM ALL_TAB_COLUMNS "
            + "WHERE TABLE_NAME = '" +tabela+ "'"
            + "AND COLUMN_NAME IN (" +colunasString+ ")";
        log.info("SQL: {}", sql);

        try(val statement = conn.prepareStatement(sql);
            var resultado = statement.executeQuery()) {

            log.info("Query executada com sucesso. Resultado obtido:");
            while(resultado.next()) {
                var nome = resultado.getString("COLUMN_NAME");
                var type = resultado.getString("DATA_TYPE");
                var metaDados = new ColumnInfo(
                    resultado.getInt("DATA_LENGTH"),
                    resultado.getInt("DATA_PRECISION"),
                    resultado.getInt("DATA_SCALE")
                );
                log.info("{}: {} - {}.", nome, type, metaDados);
                colunas.parallelStream()
                    .filter(col -> col.equals(nome))
                    .findFirst()
                    .ifPresent(col -> mapaColunas.put(col, metaDados));
                        //{
                        //col.setTipo(TipoColuna.from(type));
                        //col.setMetaDados(metaDados);
                    //});
            }
            var pendentes = colunas.parallelStream()
                .filter(col -> !mapaColunas.containsKey(col))
                .collect(Collectors.joining(", "));
            log.info("Total de filtros identificados: {}/{}", mapaColunas.size(), colunas.size());

            //TODO: criar exception própria
            if(!pendentes.isEmpty())
                throw new RuntimeException("Existem colunas não identificadas: " + pendentes);
            return mapaColunas;
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlUtils.getExceptionMainCause(e));
        }
    }

    public TableDB getColumnsFromTables(@NonNull String tabela) {
        var colunas = new ArrayList<ColumnDB>();
        var sql = "SELECT COLUMN_NAME, DATA_LENGTH, DATA_TYPE, DATA_PRECISION, DATA_SCALE "
            + "FROM ALL_TAB_COLUMNS "
            + "WHERE TABLE_NAME = '" +tabela+ "'";
        log.info("SQL: {}", sql);

        try(val statement = conn.prepareStatement(sql);
            var resultado = statement.executeQuery()) {

            log.info("Query executada com sucesso. Coletando dados da tabela.");
            while(resultado.next()) {
                var coluna = ColumnDB.builder()
                    .nome(resultado.getString("COLUMN_NAME"))
                    .tipo(TipoColuna.from(
                        resultado.getString("DATA_TYPE")
                    ))
                    .tamanho(resultado.getInt("DATA_LENGTH"))
                    .precisao(resultado.getInt("DATA_PRECISION"))
                    .escala(resultado.getInt("DATA_SCALE"))
                    .build();
                colunas.add(coluna);
            }
            log.info("Total de colunas identificadas: {}", colunas.size());
            return TableDB.builder()
                .tabela(tabela)
                .colunas(colunas)
                .build();
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlUtils.getExceptionMainCause(e));
        }
    }


    public Set<TableDB> getColumnsFromTables(@NonNull Collection<String> tabelas) {
        return getColumnsFromTables(tabelas);
    }

    public Set<TableDB> getColumnsFromTables(
        @NonNull Set<String> tabelas,
        @NonNull Set<String> colunas) {

        var tabelasConsultadas = new LinkedHashSet<TableDB>();
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

        try(val statement = conn.prepareStatement(sql);
            var resultado = statement.executeQuery()) {

            log.info("Query executada com sucesso. Coletando dados das tabelas.");
            while(resultado.next()) {
                var nomeTabela = resultado.getString("TABLE_NAME");
                var tabelaAlvo = tabelasConsultadas.parallelStream()
                    .filter(t -> t.tabela().equalsIgnoreCase(nomeTabela))
                    .findFirst()
                    .orElseGet(() -> TableDB.builder()
                        .tabela(nomeTabela)
                        .colunas(new ArrayList<>())
                        .build()
                    );
                var coluna = ColumnDB.builder()
                    .nome(resultado.getString("COLUMN_NAME"))
                    .tipo(TipoColuna.from(
                        resultado.getString("DATA_TYPE")
                    ))
                    .tamanho(resultado.getInt("DATA_LENGTH"))
                    .precisao(resultado.getInt("DATA_PRECISION"))
                    .escala(resultado.getInt("DATA_SCALE"))
                    .build();
                tabelaAlvo.colunas().add(coluna);
                tabelasConsultadas.add(tabelaAlvo);
            }
            log.info("Total de registros coletados: {}", tabelasConsultadas.size());
            return Set.copyOf(tabelasConsultadas);
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlUtils.getExceptionMainCause(e));
        }
    }

    public List<FiltroSql> findAndSetColumnInfo(
        @NonNull String tabela,
        @NonNull List<FiltroSql> filtros) {
        //----------------------------------------
        //TODO: não daria para já escrever diretamente o nome da tabela e das colunas na query?
        var colunasString = filtros.parallelStream()
            .map(FiltroSql::getColuna)
            .map(col -> "'" +col+ "'")
            .collect(Collectors.joining(", "));

        var sql = "SELECT COLUMN_NAME, DATA_LENGTH, DATA_TYPE, DATA_PRECISION, DATA_SCALE "
            + "FROM ALL_TAB_COLUMNS "
            + "WHERE TABLE_NAME = '" +tabela+ "'"
            + "AND COLUMN_NAME IN (" +colunasString+ ")";
        log.info("SQL: {}", sql);

        try(val statement = conn.prepareStatement(sql);
            var resultado = statement.executeQuery()) {

            log.info("Query executada com sucesso. Resultado obtido:");
            while(resultado.next()) {
                var col = resultado.getString("COLUMN_NAME");
                var type = resultado.getString("DATA_TYPE");
                var metaDados = new ColumnInfo(
                    resultado.getInt("DATA_LENGTH"),
                    resultado.getInt("DATA_PRECISION"),
                    resultado.getInt("DATA_SCALE")
                );
                log.info("{}: {} - {}.", col, type, metaDados);
                filtros.stream()
                    .filter(filtro -> filtro.getColuna().equals(col))
                    .findFirst()
                    .ifPresent(filtro -> {
                        filtro.setTipo(TipoColuna.from(type));
                        filtro.setMetaDados(metaDados);
                    });
            }
            var pendentes = filtros.stream()
                .filter(filtro -> filtro.getMetaDados() == null)
                .map(FiltroSql::getVariavel)
                .toList();
            var sucessos = filtros.size() - pendentes.size();
            log.info("Total de filtros identificados: {}/{}", sucessos, filtros.size());

            //TODO: criar exception própria
            if(!pendentes.isEmpty()) {
                throw new RuntimeException(
                    "Não foi possível obter todos os dados necessários. "
                    + "Colunas não identificadas: " + String.join(", ", pendentes)
                );
            }
            return filtros;
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlUtils.getExceptionMainCause(e));
        }
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
//            log.info("Inserindo registros aleatórios e validando tabelas e colunas da query.");
//            for(var queryVar : queryVars) {
//                var index = queryVar.getIndex();
//                //TODO: gerar valor aleatório STRING
//                var valor = queryVar.gerarValorAleatorio();
//                var array = queryVar.getArray();
//                if(array) {
//                    var tipo = queryVar.getTipo();
//                    var valorArray = conn.createArrayOf(tipo.name(), new Object[] {valor, valor});
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
        if(!SqlUtils.isSafeQuery(sql))
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
        if(!SqlUtils.isSafeInsertQuery(sql))
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
        if(!SqlUtils.isSafeDeleteQuery(sql))
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
