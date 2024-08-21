package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.*;
import br.com.ppw.dma.util.FormatString;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ConfigQueryService extends MasterService<Long, ConfigQuery, ConfigQueryService> {

    private final ConfigQueryRepository configQueryDao;

    @Autowired
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

    public void validadeQuery(@NonNull String query, @NonNull AmbienteAcessoDTO acessoBanco)
    throws SQLGrammarException, SQLException {
        try(val masterDao = new MasterOracleDAO(acessoBanco)) {
            if(FormatString.possuiVariaveis(query)) {
                log.info("Queries possui variáveis. Extraindo tabelas e column da query.");
                var extraction = SqlSintaxe.analyse(query);
                var tables = extraction.tables();
                var columns = extraction.filters()
                    .parallelStream()
                    .map(QueryFilter::column)
                    .collect(Collectors.toSet());

                log.info("Iniciando coleta de metadados.");
                var mapVariables = masterDao.getColumnsFromTables(tables, columns)
                    .parallelStream()
                    .map(DbTable::colunas)
                    .flatMap(Collection::parallelStream)
                    .map(DbColumn::variablesWithRandomValues)
                    .map(Map::entrySet)
                    .flatMap(Set::parallelStream)
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                    ));
                query = FormatString.substituirVariaveis(query, mapVariables);
            }
            masterDao.validadeQuery(query);
        }
    }

    public void deleteVar(@NonNull Long varId) {
        configQueryDao.deleteQueryVarById(varId);
    }

    /**
     * Cria ou atualiza uma entidade {@link ConfigQuery} com base numa query informada.
     * @param job {@link Job} a ser relacionado a entidade dessa configuração de query
     * @param query {@link QueryInfoDTO}
     * @return entidade {@link ConfigQuery}
     * @throws DuplicatedRecordException em caso de duplicidade no banco
     */
    public ConfigQuery criarAtualizar(@NonNull Job job, @NonNull QueryInfoDTO query)
    throws DuplicatedRecordException {
        if(query.getId().isPresent()) {
            var id = query.getId().get();
            log.info("Atualizando Query ID {} (Job ID {}).", id, job.getId());
            var configQuery = findById(id);
            configQuery.atualizar(query);
            configQuery.setJob(job);
            save(configQuery);
            log.info("ConfigQuery atualizada com sucesso: ID {}.", configQuery.getId());
            return configQuery;
        }
        log.info("Criando nova Query (Job ID {}).", job.getId());
        var configQuery = new ConfigQuery(query);
        configQuery.setJob(job);
        save(configQuery);
        log.info("ConfigQuery gerada com sucesso: ID {}.", configQuery.getId());
        return configQuery;
    }


}
