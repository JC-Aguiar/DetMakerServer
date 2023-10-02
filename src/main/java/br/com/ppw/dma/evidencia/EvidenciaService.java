package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.job.ComandoSql;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        val tabelasGenericas = new ArrayList<ExtrcaoBanco>();
        for(val cmdSql : comandosSql) {
            val bancoResult = extractTable(cmdSql.getTabela(), cmdSql.getTabela());
            if(bancoResult == null) continue;

            bancoResult.forEach((k, v) -> log.info("\t - {}: {}", k, v));
            tabelasGenericas.add(
                new ExtrcaoBanco(cmdSql).addResultado(bancoResult)
            );
        }
        log.info("Total de extrações dinâmicas realizadas no banco: {}.",
            tabelasGenericas.size());
        return tabelasGenericas;
    }

    //TODO: criar exception própria
    //TODO: javadoc
    private Map<String, Object> extractTable(@NotBlank String tableName, @NotBlank String whereQuery) {
        boolean isValid = validateQuery(whereQuery);
        //if(!isValid) throw new RuntimeException("A query informada têm comandos DDL não permitidos.");
        if(!isValid) {
            log.warn("A query informada têm comandos DDL não permitidos.");
            return null;
        }
        log.info("Acessando no banco os nomes dos campos da tabela '{}'.", tableName);
        //val tabelaGenerica = new ExtrcaoBanco();
        final List<String> fields = dao.getFieldsFromTable(tableName);
        return dao.getMapFromFieldsTableAndFilter(fields, tableName, whereQuery);
    }

    //TODO: javadoc
    public boolean validateQuery(String query) {
        String ddlPattern = "(?i)\\b(create|alter|drop|truncate|rename)\\b";
        // (?i) --> Case-Insensitive
        // \b --> Fronteira de Palavra. Corresponde a uma posição entre caracteres alfanuméricos e não-alfanuméricos
        //        indicando o limite entre uma palavra e outra.
        return !query.matches(".*" + ddlPattern + ".*");
    }

}
