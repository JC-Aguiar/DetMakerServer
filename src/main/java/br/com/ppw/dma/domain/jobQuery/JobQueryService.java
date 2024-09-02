package br.com.ppw.dma.domain.jobQuery;

import br.com.ppw.dma.domain.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.master.*;
import br.com.ppw.dma.exception.DuplicatedRecordException;
import br.com.ppw.dma.util.FormatString;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobQueryService extends MasterService<Long, JobQuery, JobQueryService> {

    private final JobQueryRepository jobQueryDao;

    @Autowired
    public JobQueryService(JobQueryRepository jobQueryDao) {
        super(jobQueryDao);
        this.jobQueryDao = jobQueryDao;
    }

    public List<JobQuery> findAllByJobId(@NonNull Long id) {
        return jobQueryDao.findAllByJobId(id);
    }

    public List<JobQuery> findAllByCliente(@NonNull Long clienteId) {
        val result = jobQueryDao.findAllByClienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public void validadeQuery(@NonNull String query, @NonNull AmbienteAcessoDTO acessoBanco)
    throws SQLGrammarException, SQLException {
        try(val masterDao = new MasterOracleDAO(acessoBanco)) {
            if(FormatString.possuiVariaveis(query)) {
                log.info("Queries possui variáveis. Extraindo tabelas e column da query.");
                var extraction = SqlSintaxe.analyse(query);
                var mapVariables = masterDao.extractInfoFromTables(extraction)
                    .parallelStream()
                    .map(DbTable::colunas)
                    .flatMap(Set::parallelStream)
                    .map(DbColumn::variablesWithRandomValues)
                    .map(Map::entrySet)
                    .flatMap(Set::parallelStream)
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                    ));
                query = FormatString.substituirVariaveis(query, mapVariables);
            }
            masterDao.runDQL(query);
        }
    }

    /**
     * Cria ou atualiza uma entidade {@link JobQuery} com base numa query informada.
     * @param job {@link Job} a ser relacionado a entidade dessa configuração de query
     * @param query {@link QueryInfoDTO}
     * @return entidade {@link JobQuery}
     * @throws DuplicatedRecordException em caso de duplicidade no banco
     */
    public JobQuery criarAtualizar(@NonNull Job job, @NonNull QueryInfoDTO query)
    throws DuplicatedRecordException {
        if(query.getId().isPresent()) {
            var id = query.getId().get();
            log.info("Atualizando Query ID {} (Job ID {}).", id, job.getId());
            var configQuery = findById(id);
            configQuery.atualizar(query);
            configQuery.setJob(job);
            save(configQuery);
            log.info("JobQuery atualizada com sucesso: ID {}.", configQuery.getId());
            return configQuery;
        }
        log.info("Criando nova Query (Job ID {}).", job.getId());
        var configQuery = new JobQuery(query);
        configQuery.setJob(job);
        save(configQuery);
        log.info("JobQuery gerada com sucesso: ID {}.", configQuery.getId());
        return configQuery;
    }


}
