package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

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

    public void validadeQuery(@NotBlank String sql, @NonNull AmbienteAcessoDTO acessoBanco)
    throws SQLGrammarException, SQLException {
        try(val masterDao = new MasterOracleDAO(acessoBanco)) {
            masterDao.validateQuery(sql);
        }
    }

}
