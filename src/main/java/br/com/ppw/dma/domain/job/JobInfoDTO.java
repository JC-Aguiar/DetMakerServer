package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.storage.JobPointer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.dividirValores;
import static br.com.ppw.dma.util.FormatString.valorVazio;

@Data
@Slf4j
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInfoDTO implements JobPointer {

    OffsetDateTime dataRegistro;
    Long id;
    List<String> executarAposJob = new ArrayList<>();
    String grupoConcorrencia;
    String fase;
    String nome;
    String descricao;
    String grupoUda;
    List<String> programa = new ArrayList<>();
    List<String> tabelas = new ArrayList<>();
    String servidor;
    String caminhoExec;
    List<JobParamDTO> parametros = new ArrayList<>();
    List<JobResourceDTO> mascaraEntrada = new ArrayList<>();
    List<JobResourceDTO> mascaraSaida = new ArrayList<>();
    List<JobResourceDTO> mascaraLog = new ArrayList<>();
    String tratamento;
    String escalation;
    LocalDate dataAtualizacao;
    String atualizadoPor;
    //List<ExecQueryDTO> queries = new ArrayList<>();


    public static JobInfoDTO converterJob(@NonNull Job job) {
        log.debug("Convertendo entidade Job em DTO.");
        val jobDto = new ModelMapper().map(job, JobInfoDTO.class);
        jobDto.setExecutarAposJob(dividirValores(job.getExecutarAposJob()));
        jobDto.setPrograma(dividirValores(job.getPrograma()));
        jobDto.setTabelas(dividirValores(job.getTabelas()));
        jobDto.setParametros(
            job.getListaParametros()
                .stream()
                .map(JobParamDTO::converterEntidade)
                .toList()
        );
        jobDto.setMascaraEntrada(
            job.getMascarasCarga()
                .stream()
                .map(JobResourceDTO::converterEntidade)
                .toList()
        );
        jobDto.setMascaraLog(
            job.getMascarasLog()
                .stream()
                .map(JobResourceDTO::converterEntidade)
                .toList()
        );
        jobDto.setMascaraSaida(
            job.getMascarasRemessa()
                .stream()
                .map(JobResourceDTO::converterEntidade)
                .toList()
        );
        return jobDto;
    }

    @JsonIgnore
    public String pathToJob() {
        if(!caminhoExec.endsWith("/")) caminhoExec = caminhoExec + "/";
        return caminhoExec + nome;
    }

    @JsonIgnore
    public List<String> pathLog() {
        return mascaraLog
            .stream()
            .map(JobResourceDTO::getMascara)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> pathSaida() {
        return mascaraSaida
            .stream()
            .map(JobResourceDTO::getMascara)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> pathEntrada() {
        return mascaraEntrada
            .stream()
            .map(JobResourceDTO::getMascara)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> getAllTabelas() {
        return tabelas;
    }

}
