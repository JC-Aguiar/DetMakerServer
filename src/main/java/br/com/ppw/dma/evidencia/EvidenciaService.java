package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.job.ComandoSql;
import br.com.ppw.dma.util.ExtrcaoBanco;
import br.com.ppw.dma.util.NativeSqlDAO;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EvidenciaService {

    @Autowired
    private final NativeSqlDAO dao;

    public EvidenciaService(NativeSqlDAO dao) {
        this.dao = dao;
    }

    //TODO: javadoc
    public List<ExtrcaoBanco> extractTable(@NotEmpty List<ComandoSql> comandosSql) {
        val extracoes = new ArrayList<ExtrcaoBanco>();
        for(val cmdSql : comandosSql) {
            try {
                extracoes.add(extractTable(cmdSql));
            }
            catch(Exception e) {
                e.printStackTrace();
                log.warn(e.getMessage());
            }
        }
        log.info("Total de comandos SQL realizadas: {}.", extracoes.size());
        return extracoes;
    }

    //TODO: criar exception própria
    //TODO: javadoc
    public ExtrcaoBanco extractTable(@NonNull ComandoSql sql) {
        log.info("Realizando extração da tabela '{}'.", sql.getTabela());
        boolean camposValidos = validateQuery(sql.getCampos());
        boolean tabelaValida = validateQuery(sql.getTabela());
        boolean filtroValido = validateQuery(sql.getFiltro());
        if(!camposValidos || !tabelaValida || !filtroValido) {
            //TODO: criar exception própria?
            throw new RuntimeException("A query informada contêm comandos DDL não permitidos.");
        }
        List<String> campos = null;
        if(sql.getCampos() == null || sql.getCampos().isEmpty())
            campos = dao.getFieldsFromTable(sql.getTabela());
        else
            campos = sql.getCampos();

        val resultado = dao.getFieldAndValuesFromTable(campos, sql.getTabela(), sql.getFiltro());
        return new ExtrcaoBanco(sql).addResultado(resultado);
    }

    //TODO: javadoc
    public boolean validateQuery(String query) {
        if(query == null || query.trim().isEmpty()) return true;
        String ddlPattern = "(?i)\\b(create|alter|drop|truncate|rename)\\b";
        return !query.matches(".*" + ddlPattern + ".*");
    }

    //TODO: javadoc
    public boolean validateQuery(List<String> campos) {
        if(campos == null || campos.isEmpty()) return true;
        return campos.stream().allMatch(this::validateQuery);
    }

}
