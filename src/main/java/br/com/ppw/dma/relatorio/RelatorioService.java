package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.evidencia.EvidenciaProcess;
import br.com.ppw.dma.job.JobInfoDTO;
import br.com.ppw.dma.job.JobPreparation;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.pipeline.Pipeline;
import br.com.ppw.dma.pipeline.PipelinePreparation;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static java.time.ZoneOffset.UTC;

@Service
@Slf4j
public class RelatorioService extends MasterService<Long, Relatorio, RelatorioService> {

    @Autowired
    private final RelatorioRepository dao;


    public RelatorioService(RelatorioRepository dao) {
        super(dao);
        this.dao = dao;
    }

    public List<Relatorio> findAllByAmbiente(@NonNull Long clienteId) {
        val result = dao.findAllByAmbienteId(clienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public Page<Relatorio> findAllByAmbiente(@NonNull Long ambienteId, @NonNull Pageable pageConfig) {
        val result = dao.findAllByAmbienteId(ambienteId, pageConfig);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public List<Relatorio> findAllFromPipeline(@NonNull Pipeline pipeline) {
//        log.info("Obtendo Relatórios no banco relacionados a Ambiente '{}'. ", pipeline.getProps().getNome());
        log.info("Obtendo Relatórios no banco relacionados a Ambiente '{}'. ", pipeline.getNome());
        val relatorios = dao.findAllByPipeline(pipeline);
        log.info("Total de Relatórios identificados: {}.", relatorios.size());
        relatorios.forEach(r -> log.info(r.toString()));
        return relatorios;
    }

    public Relatorio salvarRelatorioRevisado(@NonNull RelatorioRevisadoDTO dto) {
        log.info("Atualizando campo 'considerações' do Relatório ID {}", dto.getId());
        val relatorio = findById(dto.getId());
        relatorio.setConsideracoes(dto.getConsideracoes());
        return persist(relatorio);
    }

    public Relatorio findMostRecentFromPipeline(@NonNull Pipeline pipeline) {
        val relatorios = findAllFromPipeline(pipeline);

        log.info("identificando Relatório mais recente");
        val relatorioMaisRecente = relatorios.stream()
            .min(Comparator.comparing(Relatorio::getData))
            .orElseThrow();

        log.info("Relatório identificado:");
        log.info(relatorioMaisRecente.toString());
        return relatorioMaisRecente;
    }

    @Transactional
    public Relatorio buildAndPersist(
        @NonNull PipelinePreparation preparation,
        @NotNull List<EvidenciaProcess> evidencias) {

        log.debug("Separando Evidências persistidas com sucesso daquelas com erro.");
        var consideracoes = new StringBuilder();
        var evidenciasOk = new ArrayList<Evidencia>();
        for(var ev : evidencias) {
            if(ev.exception()) consideracoes.append(ev.detalhes() + "\n");
            else ev.evidencia().ifPresent(evidenciasOk::add);
        }
        log.info("Convertendo Relatório DTO em Entidade.");
        var relatorio = Relatorio.builder()
            .nomeAtividade(preparation.relatorio().getNomeAtividade())
            .consideracoes(consideracoes.toString())
            .cliente(preparation.ambiente().getCliente().getNome())
            .ambiente(preparation.ambiente())
            .pipeline(preparation.pipeline())
            .evidencias(evidenciasOk)
//            .parametros(parametrosDosJobs)
            .data(LocalDate.now(RELOGIO))
            .dataCompleta(OffsetDateTime.now(RELOGIO))
            .build();
        relatorio.setIdProjeto(preparation.relatorio().getIdProjeto());
        relatorio.setNomeProjeto(preparation.relatorio().getNomeProjeto());
        relatorio.setTesteTipo(TiposDeTeste
            .identificar(preparation.relatorio().getTesteTipo())
            .orElse(null)
        );
        relatorio = persist(relatorio);
        return relatorio;
    }

    @Transactional
    public Relatorio persist(@NotNull Relatorio relatorio) {
        log.info("Persistindo Relatório no banco:");
        log.info(relatorio.toString());

        relatorio = dao.save(relatorio);
        log.info("Relatório ID {} gravado com sucesso.", relatorio.getId());
        return relatorio;
    }

    public Page<Relatorio> findAllByExample(Example<Relatorio> exemplo, Pageable pageConfig) {
        return dao.findAll(exemplo, pageConfig);
    }

}
