package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.master.MasterService;
import br.com.ppw.dma.pipeline.Pipeline;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;

@Service
@Slf4j
public class RelatorioService extends MasterService<Long, Relatorio, RelatorioService> {

    @Autowired
    private final RelatorioRepository dao;


    public RelatorioService(RelatorioRepository dao) {
        super(dao);
        this.dao = dao;
    }

    public List<Relatorio> findAllByAmbiente(@NonNull Long ambienteId) {
        val result = dao.findAllByAmbienteId(ambienteId);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public Page<Relatorio> findAllByAmbiente(@NonNull Pageable pageConfig, @NonNull Long ambienteId) {
        val result = dao.findAllByAmbienteId(ambienteId, pageConfig);
        if(result.isEmpty()) throw new NoSuchElementException();
        return result;
    }

    public List<Relatorio> findAllFromPipeline(@NonNull Pipeline pipeline) {
        log.info("Obtendo Relatórios no banco relacionados a Ambiente '{}'. ", pipeline.getProps().getNome());
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

//    public Relatorio cloneRelatorioFromPipeline(@NonNull Ambiente pipeline) {
//        val relatorio = findAllByPipeline(pipeline)
//            .stream()
//            .min(Comparator.comparing(Relatorio::getDataInicio))
//            .orElseThrow();
//        log.info("Criando novo Relatório clone ao ID {}.", relatorio.getId());
//        val novoRelatorio = Relatorio.builder()
//            .
//            .build();
//    }

    @Transactional
    public Relatorio buildAndPersist(
        @NotNull Ambiente ambiente,
        @NotNull RelatorioInfoDTO dto,
        @NotNull Pipeline pipeline,
        @NotNull List<Evidencia> evidencias,
        String parametros) {
        //--------------------------------------
        log.info("Convertendo Relatório DTO em Entidade.");
        val dataInicio = evidencias.stream()
            .map(Evidencia::getDataInicio)
            .min(OffsetDateTime::compareTo)
            .orElse(null);
        val dataFim = evidencias.stream()
            .map(Evidencia::getDataFim)
            .max(OffsetDateTime::compareTo)
            .orElse(null);

        var relatorio = Relatorio.builder()
            .nomeAtividade(dto.getNomeAtividade())
            .consideracoes(dto.getConsideracoes())
            .cliente(ambiente.getCliente().getNome())
            .ambiente(ambiente)
            .pipeline(pipeline)
            .evidencias(evidencias)
            .parametros(parametros)
            .data(OffsetDateTime.now(RELOGIO))
            .build();

        relatorio.setIdProjeto(dto.getIdProjeto());
        relatorio.setNomeProjeto(dto.getNomeProjeto());
        relatorio.setTesteTipo(
            TiposDeTeste.identificar(dto.getTesteTipo()).orElse(null)
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

}
