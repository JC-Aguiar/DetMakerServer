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

import static br.com.ppw.dma.util.FormatString.*;

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
    List<String> parametros = new ArrayList<>();
    List<String> descricaoParametros = new ArrayList<>();
    String diretorioEntrada;
    List<String> mascaraEntrada = new ArrayList<>();
    String diretorioSaida;
    List<String> mascaraSaida = new ArrayList<>();
    String diretorioLog;
    List<String> mascaraLog = new ArrayList<>();
    String tratamento;
    String escalation;
    LocalDate dataAtualizacao;
    String atualizadoPor;
    //List<QueryInfoDTO> queries = new ArrayList<>();


    public static JobInfoDTO converterJob(@NonNull Job job) {
        log.info("Convertendo entidade Job em DTO.");
        val jobDto = new ModelMapper().map(job, JobInfoDTO.class);
        jobDto.setExecutarAposJob(dividirValores(job.getExecutarAposJob()));
        jobDto.setPrograma(dividirValores(job.getPrograma()));
        jobDto.setParametros(dividirValores(job.getParametros()));
        jobDto.setDescricaoParametros(dividirValores(job.getDescricaoParametros()));
        jobDto.setTabelas(dividirValores(job.getTabelas()));
        jobDto.setMascaraEntrada(dividirValores(job.getMascaraEntrada()));
        jobDto.setMascaraSaida(dividirValores(job.getMascaraSaida()));
        jobDto.setMascaraLog(dividirValores(job.getMascaraLog()));
        //jobDto.setQueries(QueryInfoDTO.getFromJob(job));
//        log.info("Conversão realizada com sucesso.");
//        log.info("{}", jobDto);
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
            .map(mascara -> montarPath(diretorioLog, extrairMascara(mascara)))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> pathSaida() {
        return mascaraSaida
            .stream()
            .map(mascara -> montarPath(diretorioSaida, extrairMascara(mascara)))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> pathEntrada() {
        return mascaraEntrada
            .stream()
            .map(mascara -> montarPath(diretorioEntrada, extrairMascara(mascara)))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> getAllTabelas() {
        return tabelas;
    }

    private static String montarPath(String diretorio, String nome) {
        diretorio = valorVazio(diretorio);
        nome = valorVazio(nome);
        if(diretorio.isEmpty() || nome.isEmpty())
            return "";
        if(!diretorio.endsWith("/"))
            diretorio = diretorio + "/";
        return diretorio + nome;
    }

}
