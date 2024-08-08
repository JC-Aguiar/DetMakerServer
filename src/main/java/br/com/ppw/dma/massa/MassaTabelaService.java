package br.com.ppw.dma.massa;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.master.MasterOracleDAO;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.master.MasterSummary;
import br.com.ppw.dma.util.FormatDate;
import br.com.ppware.api.GeradorDeMassa;
import br.com.ppware.api.MassaPreparada;
import jakarta.persistence.PersistenceException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.InvalidAttributeValueException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.SQL_BRASIL_STYLE;

@Slf4j
@Service
public class MassaTabelaService extends MasterService<Long, MassaTabela, MassaTabelaService> {

    @Autowired
    private final MassaTabelaRepository massaTabelaDao;

    @Autowired
    private final MassaColunaRepository massaColunaDao;

    @Autowired
    private final AmbienteService ambienteService;


    public MassaTabelaService(
        MassaTabelaRepository massaTabelaDao,
        MassaColunaRepository massaColunaDao,
        AmbienteService ambienteService) {
        //-------------------------------------
        super(massaTabelaDao);
        this.massaTabelaDao = massaTabelaDao;
        this.massaColunaDao = massaColunaDao;
        this.ambienteService = ambienteService;
    }

    public List<MassaTabela> findAllByCliente(@NonNull Long clienteId) {
        val result = massaTabelaDao.findAllByClienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public List<MassaTabela> findByClienteIdAndNomes(
        @NonNull Long clienteId,
        @NonNull Set<String> nomes) {

        if(nomes.isEmpty()) return List.of();
        val result = massaTabelaDao.findByClienteIdAndNomes(clienteId, nomes);
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
     * As {@link MassaPreparada}s são armazenados temporariamente no {@link MasterSummary} com base no seu
     * resultado.
     * @param ambienteBanco {@link AmbienteAcessoDTO} contendo o apontamento e a autenticação do banco que iremos
     *                      nos conectar
     * @param dtos um ou mais {@link MassaTabelaDTO} das tabelas a serem processadas
     * @return resumo da operação realizada, retornada na forma de um {@link MasterSummary}
     */
    public MasterSummary<MassaPreparada> newInserts(
        @NonNull AmbienteAcessoDTO ambienteBanco,
        @NonNull List<MassaTabelaDTO> dtos) {

        return newInserts(ambienteBanco, dtos.toArray(new MassaTabelaDTO[0]));
    }

    public MasterSummary<MassaPreparada> newInserts(
        @NonNull AmbienteAcessoDTO ambienteBanco,
        @NonNull MassaTabelaDTO...dtos) {

        var massas = GeradorDeMassa.mapearMassa(SQL_BRASIL_STYLE, dtos);
        var summary = MasterSummary.startsNegative(massas);

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
//        finally {
//            summary.getSaved().forEach(GeradorDeMassa::addNovaMassa);
//        }
    }

    public MasterSummary<MassaPreparada> delete(
        @NonNull AmbienteAcessoDTO ambienteBanco,
        @NonNull MassaPreparada...massas) {

        var summary = MasterSummary.startsNegative(List.of(massas));

        //Tenta executar o delete da massa gerada dinamicamente
        try(var oracleDao = new MasterOracleDAO(ambienteBanco)) {
            for(var massa : massas) {
                log.info("Massa: {}", massa);
                summary.tryAndSet(massa, oracleDao::deleteSql);
            }
            return summary;
        }
        //Exceção em caso de exception inesperado
        catch(SQLException | PersistenceException e) {
            e.printStackTrace();
            return summary;
        }
        //Salva no contexto estático do GeradprDeMassa todas as massas inseridas com sucesso
//        finally {
//            summary.getSaved().forEach(GeradorDeMassa::addNovaMassa);
//        }
    }

    public List<MassaPreparada> mockMassa(
        @NonNull AmbienteAcessoDTO banco,
        @NonNull MassaTabelaDTO dto) {

        var colunas = dto.getColunas()
            .parallelStream()
            .map(MassaColunaDTO::getNome)
            .collect(Collectors.toSet());

        ambienteService.getSqlDataTypes(dto.getNome(), colunas, banco)
            .forEach((nome, info) ->
                dto.getColunas().parallelStream()
                    .filter(col -> col.getNome().equals(nome))
                    .findFirst()
                    .ifPresent(col -> col.setMetadados(info))
            );
        return GeradorDeMassa.mapearMassa(FormatDate.BRASIL_STYLE, dto);
    }

    //TODO: javadoc
    public List<MassaTabela> getAllByClienteAndNomes(
        @NonNull Cliente cliente,
        @NonNull Set<String> massasNome)
    throws InvalidAttributeValueException {
        log.info("Obtendo e validando as massas {} do cliente '{}'.", massasNome, cliente.getNome());
        var massas = findByClienteIdAndNomes(cliente.getId(), massasNome);
        var massasPendentes = massasNome
            .parallelStream()
            .filter(nome ->  massas.stream().noneMatch(massa -> massa.getNome().equals(nome)))
            .toList();
        if(massasPendentes.isEmpty()) return massas;
        throw new InvalidAttributeValueException(
            "Massas não identificadas para o Cliente '" +
            cliente.getNome() +
            "': " +
            String.join(", ", massasPendentes)
        );
    }

//    /**
//     * Converte um {@link MasterSummary} do tipo {@link MassaPreparada} para o tipo {@link String},
//     * onde a identificação dos registros é feita pelo atributo {@link MassaPreparada#getNome()}
//     * @param summaryInserts {@link MasterSummary}<{@link MassaPreparada}> da massa processada
//     * @return {@link MasterSummary}<{@link String}> com os respectivos identificadores
//     */
//    public MasterSummary<String> parseSummary(@NonNull MasterSummary<MassaPreparada> summaryInserts) {
//        var retorno = new MasterSummary<String>();
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
