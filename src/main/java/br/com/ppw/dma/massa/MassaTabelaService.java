package br.com.ppw.dma.massa;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.master.MasterSummaryDTO;
import br.com.ppware.api.GeradorDeMassa;
import br.com.ppware.api.MassaPreparada;
import br.com.ppware.api.MassaTabelaDTO;
import jakarta.persistence.PersistenceException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;

import static br.com.ppw.dma.util.FormatDate.SQL_BRASIL_STYLE;

@Service
@Slf4j
public class MassaTabelaService extends MasterService<Long, MassaTabela, MassaTabelaService> {

    @Autowired
    private final MassaTabelaRepository massaTabelaDao;

    @Autowired
    private final MassaColunaRepository massaColunaDao;


    public MassaTabelaService(
        MassaTabelaRepository massaTabelaDao,
        MassaColunaRepository massaColunaDao) {
        //-------------------------------------
        super(massaTabelaDao);
        this.massaTabelaDao = massaTabelaDao;
        this.massaColunaDao = massaColunaDao;
    }

    public List<MassaTabela> findAllByCliente(@NonNull Long clienteId) {
        val result = massaTabelaDao.findAllByClienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public MassaTabela save(@NonNull Cliente cliente, @NonNull MassaTabelaDTO dto) {
        log.info("Salvando nova massa para tabela '{}'.", dto.getNome());
        log.info("Convertendo DTO para Entidade.");
        var massa = new MassaTabela(dto);
        massa.setCliente(cliente);
        log.info("Entidade gerada:");
        log.info(massa.toString());

        log.info("Salvando mapeamento da tabela '{}' no banco.", dto.getNome());
        massa = massaTabelaDao.save(massa);

//        log.info("Salvando mapeamento das colunas da tabela '{}' no banco.", dto.getNome());
//        massa.getColunas()
//            .stream()
//            .map(massaColunaDao::saveAndFlush)
//            .forEach(col -> log.info(col.toString()));

        log.info(massa.toString());
        return massa;
    }

    /**
     * Inteligência que busca gerenciar a execução dos inserts das massas de dados geradas aleatoriamente.
     * Primeiro se converte os DTOs em objetos do tipo {@link MassaPreparada}, para depois tentar processar
     * um a um suas respectivas queries no banco usando {@link DriverManager#getConnection(String, String, String)}
     * que já consta preparado dentro do {@link MasterOracleDAO}.
     * As {@link MassaPreparada}s são armazenados temporariamente no {@link MasterSummaryDTO} com base no seu
     * resultado. No final, transferimos as massas cujos inserts foram realizados com sucesso para a lista estática
     * 'MASSA_A_DELETAR', controlada pelo {@link GeradorDeMassa}.
     * @param ambienteBanco {@link AmbienteAcessoDTO} contendo o apontamento e a autenticação do banco que iremos
     *                      nos conectar
     * @param dtos um ou mais {@link MassaTabelaDTO} das tabelas a serem processadas
     * @return resumo da operação realizada, retornada na forma de um {@link MasterSummaryDTO}
     */
    public MasterSummaryDTO<MassaPreparada> newInserts(AmbienteAcessoDTO ambienteBanco, MassaTabelaDTO...dtos) {
        var massas = GeradorDeMassa.mapearMassa(SQL_BRASIL_STYLE, dtos);
        var summary = MasterSummaryDTO.startsNegative(massas);

        //Tenta executar o insert da massa gerada dinamicamente
        try(var oracleDao = new MasterOracleDAO(ambienteBanco)) {
            for(var massa : massas) {
                log.info("Massa: {}", massa.toString());
                summary.tryAndSet(massa, oracleDao::insertSql);
            }
            return summary;
        }
        //Exceção em caso de exception inesperado
        catch(SQLException | PersistenceException e) {
            e.printStackTrace();
            return summary;
        }
        //Salva no contexto estático do GeradprDeMassa todas as massas inseridas com sucesso
        finally {
            summary.getSaved().forEach(GeradorDeMassa::addNovaMassa);
        }
    }

//    /**
//     * Converte um {@link MasterSummaryDTO} do tipo {@link MassaPreparada} para o tipo {@link String},
//     * onde a identificação dos registros é feita pelo atributo {@link MassaPreparada#getNome()}
//     * @param summaryInserts {@link MasterSummaryDTO}<{@link MassaPreparada}> da massa processada
//     * @return {@link MasterSummaryDTO}<{@link String}> com os respectivos identificadores
//     */
//    public MasterSummaryDTO<String> parseSummary(@NonNull MasterSummaryDTO<MassaPreparada> summaryInserts) {
//        var retorno = new MasterSummaryDTO<String>();
//        summaryInserts.getSaved().forEach(massa -> retorno.save(massa.getNome()));
//        summaryInserts.getFailed().forEach(massa -> retorno.fail(massa.getNome()));
//
//        log.info("Status do resumo dos inserts: {}", retorno.getStatus().name());
//        if(!retorno.getSaved().isEmpty()) {
//            log.info("Total de massas inseridas com sucesso: {}", retorno.getSaved().size());
//            log.info(" - {}", String.join(", ", retorno.getSaved()));
//        }
//        if(!retorno.getFailed().isEmpty()) {
//            log.info("Total de massas não com inseridas: {}", retorno.getFailed().size());
//            log.info(" - {}", String.join(", ", retorno.getFailed()));
//        }
//        return retorno;
//    }

}
