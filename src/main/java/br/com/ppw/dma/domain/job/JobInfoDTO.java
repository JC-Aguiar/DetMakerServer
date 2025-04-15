package br.com.ppw.dma.domain.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInfoDTO {

    @Positive
    Long id;

    @NotNull
    @Size(min = 1, max = 100)
    String nome;

    @NotNull
    @Size(min = 1, max = 500)
    String descricao;

    @NotNull
    @Size(min = 1, max = 300)
    String caminhoExec;

    List<JobParamDTO> parametros = new ArrayList<>();

    List<JobResourceDTO> mascaraEntrada = new ArrayList<>();

    List<JobResourceDTO> mascaraSaida = new ArrayList<>();

    List<JobResourceDTO> mascaraLog = new ArrayList<>();

    @Size(min = 1, max = 25)
    String tratamento;

    @Size(min = 1, max = 25)
    String escalation;

    OffsetDateTime dataRegistro;

    LocalDate dataAtualizacao;

    String atualizadoPor;


    //List<ExecQueryDTO> queries = new ArrayList<>();


    public static JobInfoDTO converterJob(@NonNull Job job) {
        log.debug("Convertendo entidade Job em DTO.");
        val jobDto = new ModelMapper().map(job, JobInfoDTO.class);
        jobDto.setParametros(
            job.getParametros()
                .stream()
                .map(JobParamDTO::new)
                .toList()
        );
        jobDto.setMascaraEntrada(
            job.getCargas()
                .stream()
                .map(JobResourceDTO::new)
                .toList()
        );
        jobDto.setMascaraLog(
            job.getLogs()
                .stream()
                .map(JobResourceDTO::new)
                .toList()
        );
        jobDto.setMascaraSaida(
            job.getRemessas()
                .stream()
                .map(JobResourceDTO::new)
                .toList()
        );
        log.info(jobDto.toString());
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
            .map(JobResourceDTO::getEndereco)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> pathSaida() {
        return mascaraSaida
            .stream()
            .map(JobResourceDTO::getEndereco)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> pathEntrada() {
        return mascaraEntrada
            .stream()
            .map(JobResourceDTO::getEndereco)
            .collect(Collectors.toList());
    }

}
