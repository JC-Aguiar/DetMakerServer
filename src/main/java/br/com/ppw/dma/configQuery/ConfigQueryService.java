package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.util.FormatString;
import br.com.ppw.dma.util.SqlUtils;
import jakarta.persistence.PersistenceException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class ConfigQueryService extends MasterService<Long, ConfigQuery, ConfigQueryService> {

    @Autowired
    private final ConfigQueryRepository configQueryDao;


    public ConfigQueryService(ConfigQueryRepository configQueryDao) {
        super(configQueryDao);
        this.configQueryDao = configQueryDao;
    }

    public List<ConfigQuery> findAllByJobId(@NonNull Long id) {
        return configQueryDao.findAllByJobId(id);
    }

    public List<ConfigQuery> findAllByCliente(@NonNull Long clienteId) {
        val result = configQueryDao.findAllByClienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public void validadeQuery(@NonNull String sql, @NonNull AmbienteAcessoDTO acessoBanco)
    throws SQLGrammarException, SQLException {
        try(val masterDao = new MasterOracleDAO(acessoBanco)) {
            masterDao.validadeQuery(sql);
        }
    }

    public void validadeQuery(@NonNull ComandoSql comando, @NonNull AmbienteAcessoDTO acessoBanco)
    throws SQLGrammarException, SQLException {
        var sql = FormatString.substituirVariaveis(comando.getSql(), comando.getValores());
        try(val masterDao = new MasterOracleDAO(acessoBanco)) {
            masterDao.validadeQuery(sql);
        }
    }
    
    public List<ConfigQueryVar> createValidateVariables(
        @NonNull ComandoSql comando,
        @NonNull AmbienteAcessoDTO banco) {
        //--------------------------------
        try(val masterDao = new MasterOracleDAO(banco)) {
            log.info("Obtendo metadados das variáveis.");
            comando.mapFiltrosPorTabela().forEach(masterDao::findAndSetColumnInfo);

            log.info("Convertendo para ConfigQueryVars.");
            var queryVars = comando.getFiltros()
                .stream()
                .map(ConfigQueryVar::new)
                .peek(vars -> log.info(vars.toString()))
                .toList();

            log.info("Criando valores aleatórios para testar as variáveis da query.");
            var mapaVariavelValor = ConfigQueryVar.mapaDasVariaveis(queryVars);
            var sql = FormatString.substituirVariaveis(comando.getSql(), mapaVariavelValor);
            masterDao.validadeQuery(sql);
            return queryVars;
        }
        catch(SQLException | PersistenceException e) {
            throw new RuntimeException(SqlUtils.getExceptionMainCause(e));
        }
    }

    public List<FiltroSql> getVarsFromQueryId(@NonNull Long id) {
        return configQueryDao.findAllVarsByQueryId(id)
            .stream()
            .map(FiltroSql::new)
            .toList();
    }

    public void deleteVar(@NonNull Long varid) {
        configQueryDao.deleteQueryVarById(varid);
    }

    public ConfigQuery criarAtualizar(
        @NonNull Job job,
        @NonNull ComandoSql comando,
        @NonNull List<ConfigQueryVar> queryVars)
    throws DuplicatedRecordException {

        ConfigQuery configQuery;
        if(comando.getId() != null) {
            log.info("Atualizando ConfigQuery [{}] do Job [{}] '{}'.",
                comando.getId(), job.getId(), job.getNome()
            );
            configQuery = findById(comando.getId());
            configQuery.setNome(comando.getNome());
            configQuery.setDescricao(comando.getDescricao());
            configQuery.setSql(comando.getSql());
            configQuery.setVariaveis(queryVars);
            configQuery.setJob(job);
            save(configQuery);
            log.info("ConfigQuery atualizada com sucesso. ID {}.", configQuery.getId());
        }
        else {
            log.info("Criando nova ConfigQuery para Job [{}] '{}'.", job.getId(), job.getNome());
            configQuery = new ConfigQuery(comando);
            configQuery.setVariaveis(queryVars);
            configQuery.setJob(job);
            save(configQuery);
            log.info("ConfigQuery gerada com sucesso. ID {}.", configQuery.getId());
        }
        return configQuery;
    }
}
