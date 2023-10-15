package br.com.ppw.dma.job;

import br.com.ppw.dma.master.MasterResponseDTO;
import br.com.ppw.dma.system.ShellPointer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.extrairMascara;
import static br.com.ppw.dma.util.FormatString.valorVazio;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobInfoDTO implements ShellPointer, MasterResponseDTO {

    OffsetDateTime dataRegistro;
    Long id;
    String plano;
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


    @JsonIgnore
    public String pathShell() {
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
