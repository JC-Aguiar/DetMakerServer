package br.com.ppw.dma.domain.ambiente;

import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.master.*;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.*;

@Service
@Slf4j
public class AmbienteService {

    @Autowired private AmbienteRepository dao;

    /**
     * The proxy is a static method that returns a singleton instance of the service
     *
     * @return The proxy object.
     */
    protected final AmbienteService proxy() {
        return (AmbienteService) AopContext.currentProxy();
    }

    public Optional<Ambiente> addOne(@NotNull Ambiente entity) throws DuplicatedRecordException {
        try {
            return Optional.of(dao.save(entity));
        }
        catch(ConstraintViolationException e) {
            throw new DuplicatedRecordException();
        }
    }

    // A method that returns an entity by id.
    public Ambiente findById(@NonNull Long id) {
        val record = dao.findById(id).orElseThrow();
        log.info("Registro encontrado no banco para ID {}:", id);
        log.info(record.toString());
        return record;
    }

    // A proxy method that calls `pageCheck` method.
    public List<Ambiente> findAll() {
        return dao.findAll();
    }

    // A method that validates the page.
    public Page<Ambiente> pageCheck(@NonNull Page<Ambiente> page) {
        page.stream().map(Objects::nonNull).findFirst().orElseThrow();
        return page;
    }

    // A proxy method that calls `pageCheck` method.
    public Page<Ambiente> findAll(@NonNull Pageable pageable) {
        return proxy().pageCheck(dao.findAll(pageable));
    }

    public List<Ambiente> findAllById(List<Long> ids) {
        return dao.findAllById(ids);
    }

    public List<Ambiente> findAllFromCliente(@NonNull Cliente cliente) {
        log.info("Obtendo Ambientes no banco do Cliente '{}'. ", cliente.getNome());
        val ambientes = dao.findAllByCliente(cliente);
        log.info("Total de Ambientes identificados: {}.", ambientes.size());
        ambientes.forEach(amb -> log.info(" - '{}'", amb.getNome()));
        return ambientes;
    }

    @Transactional
    public Ambiente persist(@NonNull Ambiente ambiente) {
        log.info("Persistindo Ambiente no banco:");
        log.info(ambiente.toString());

        ambiente = dao.save(ambiente);
        log.info("Ambiente ID {} gravado com sucesso.", ambiente.getId());
        return ambiente;
    }

    public Optional<Ambiente> getByName(@NonNull String nome) {
        log.info("Consultando pelo Ambiente '{}'.", nome);
        val pipeline = Optional.ofNullable(dao.findAllByNome(nome));
        if(pipeline.isPresent())
            log.info("Ambiente '{}' obtida com sucesso.", nome);
        else
            log.info("Ambiente '{}' não encontrada.", nome);
        return pipeline;
    }

    public boolean checkByName(@NonNull String nome) {
        log.info("Validando se a Ambiente '{}' existe no banco.", nome);
        val resutlado = dao.existsByNome(nome);
        log.info("Resultado: {}.", resutlado);
        return resutlado;
    }

    //Obtêm todos os metadados do banco remoto para as colunas de uma tabela
    public Optional<DbTable> getMetadatasFromTables(
        @NonNull String tabela,
        @NonNull Set<String> campos,
        @NonNull Ambiente ambiente)
    {
        return getMetadatasFromTables(tabela, campos, ambiente.acessoBanco());
    }

    //Obtêm todos os metadados do banco remoto para as colunas de uma tabela
    public Optional<DbTable> getMetadatasFromTables(
        @NonNull String tabela,
        @NonNull Set<String> campos,
        @NonNull AmbienteAcessoDTO ambiente)
    {
        try(val masterDao = new MasterOracleDAO(ambiente)) {
            var result = masterDao.extractInfoFromTables(Set.of(tabela), campos);
            var hasContent = !result.isEmpty();
            return Optional.ofNullable(hasContent ? result.get(0) : null);

        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
        }
    }

    //Obtêm todos os metadados do banco remoto para determinado conjunto de tabelas e colunas
    public List<DbTable> getMetadatasFromTables(
        @NonNull Set<String> tabelas,
        @NonNull Set<String> colunas,
        @NonNull Ambiente ambiente)
    {
        return getMetadatasFromTables(tabelas, colunas, ambiente.acessoBanco());
    }

    //Obtêm todos os metadados do banco remoto para determinado conjunto de tabelas e colunas
    public List<DbTable> getMetadatasFromTables(
        @NonNull Set<String> tabelas,
        @NonNull Set<String> colunas,
        @NonNull AmbienteAcessoDTO ambiente)
    {
        try(val masterDao = new MasterOracleDAO(ambiente)) {
            return masterDao.extractInfoFromTables(tabelas, colunas);
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
        }
    }

    /**
     * Com base nas queries informadas, é coletado todos os nomes das tabelas e column para então se
     * conectar no banco remoto e obter os respectivos metadados.
     * @param queries {@link Set} {@link String} contendo todas as queries
     * @param ambiente {@link Ambiente} contendo os dados de conexão banco
     * @return {@link Set} {@link DbTable} das tabelas obtidas e suas respectivas column ({@link DbColumn})
     * @see AmbienteService#getMetadatasFromQueries(Set, AmbienteAcessoDTO)
     * @see AmbienteService#getMetadatasFromTables(Set, Set, AmbienteAcessoDTO)
     */
    public List<DbTable> getMetadatasFromQueries(@NonNull Set<String> queries, @NonNull Ambiente ambiente) {
        return getMetadatasFromQueries(queries, ambiente.acessoBanco());
    }

    /**
     * Com base nas queries informadas, é coletado todos os nomes das tabelas e column para então se
     * conectar no banco remoto e obter os respectivos metadados.
     * @param queries {@link Set} {@link String} contendo todas as queries
     * @param ambiente {@link AmbienteAcessoDTO} contendo os dados de conexão banco
     * @return {@link Set} {@link DbTable} das tabelas obtidas e suas respectivas column ({@link DbColumn})
     * @see AmbienteService#getMetadatasFromTables(Set, Set, AmbienteAcessoDTO)
     */
    public List<DbTable> getMetadatasFromQueries(@NonNull Set<String> queries, @NonNull AmbienteAcessoDTO ambiente) {
        log.info("Queries a coletar metadados:");
        var tables = new HashSet<String>();
        var columns = new HashSet<String>();
        queries.stream()
            .peek(log::info)
            .parallel()
            .map(SqlSintaxe::analyse)
            .forEach(extraction -> {
                tables.addAll(extraction.tables());
                extraction.filters()
                    .parallelStream()
                    .map(QueryFilter::column)
                    .forEach(columns::add);
            });
        return getMetadatasFromTables(tables, columns, ambiente);
    }

    //Valida e executa (com ROWNUM = 1 e sem commit) qualquer tipo de query
    public void validadeQuerySQL(@NonNull Set<String> queries, @NonNull Ambiente ambiente) {
        try(val masterDao = new MasterOracleDAO(ambiente)) {
            queries.parallelStream().forEach(query -> {
                try {
                    masterDao.runSQL(query);
                }
                catch(SQLException | PersistenceException e) {
                    throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
                }
            });
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
        }
    }

    //Valida e executa (com ROWNUM = 1) queries DQL (de consulta)
    public void validadeQueryDQL(@NonNull Set<String> queries, @NonNull Ambiente ambiente)  {
        validadeQueryDQL(queries, ambiente.acessoBanco());
    }

    //Valida e executa (com ROWNUM = 1) queries DQL (de consulta)
    public void validadeQueryDQL(@NonNull Set<String> queries, @NonNull AmbienteAcessoDTO ambiente) {
        try(val masterDao = new MasterOracleDAO(ambiente)) {
            queries.parallelStream().forEach(query -> {
                try {
                    masterDao.testDQL(query);
                }
                catch(SQLException | PersistenceException e) {
                    throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
                }
            });
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
        }
    }

    //Valida e executa (com ROWNUM = 1 e sem commit) queries DML (de registro)
    public void validadeQueryDML(@NonNull Set<String> queries, @NonNull Ambiente ambiente) {
        validadeQueryDML(queries, ambiente.acessoBanco());
    }

    //Valida e executa (com ROWNUM = 1 e sem commit) queries DQL (de registro)
    public void validadeQueryDML(@NonNull Set<String> queries, @NonNull AmbienteAcessoDTO ambiente) {
        try(val masterDao = new MasterOracleDAO(ambiente)) {
            queries.parallelStream().forEach(query -> {
                try {
                    masterDao.testDML(query);
                }
                catch(SQLException | PersistenceException e) {
                    throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
                }
            });
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlSintaxe.getExceptionMainCause(e));
        }
    }

    //Executa query
    public void runQuery(@NonNull Set<String> queries, @NonNull AmbienteAcessoDTO ambiente) {
        try(val masterDao = new MasterOracleDAO(ambiente)) {
            queries.parallelStream().forEach(query -> {
                try {
                    masterDao.runSQL(query);
                }
                catch(SQLException | PersistenceException e) {
                    log.error(SqlSintaxe.getExceptionMainCause(e));
                }
            });
        }
        catch(SQLException | PersistenceException e) {
            log.error(SqlSintaxe.getExceptionMainCause(e));
        }
    }

}
