package br.com.ppw.dma.agenda;

import br.com.ppw.dma.batch.PlanilhaTitulo;
import br.com.ppw.dma.batch.ShellPointer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.*;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AgendaDTO implements ShellPointer {

    OffsetDateTime dataRegistro;

    String autorRegistro;

    String origemRegistro;

    String nomeArquivo;
    
    String nomePlanilha;

    @PlanilhaTitulo("ID")
    Long id;

    @PlanilhaTitulo("Executar após o Job:")
    String executarAposJob;

    @PlanilhaTitulo("Grupo de Concorrência")
    String grupoConcorrencia;

    @PlanilhaTitulo("Fase")
    String fase;

    @PlanilhaTitulo("JOB")
    String job;

    @PlanilhaTitulo("Descrição")
    String descricao;

    @PlanilhaTitulo("Grupo (UDAx)")
    String grupoUda;

    @PlanilhaTitulo("Programa")
    String programa;

    @PlanilhaTitulo("Tabelas Atualizadas")
    String tabelas;

    @PlanilhaTitulo("Servidor")
    String servidor;

    @PlanilhaTitulo("Caminho de Execução")
    String caminhoExec;

    @PlanilhaTitulo("Parâmetros")
    String parametros;

    @PlanilhaTitulo("Descrição dos ParÂmetros")
    String descricaoParametros;

    @PlanilhaTitulo("Caminho Arquivo de Entrada")
    String diretorioEntrada;

    @PlanilhaTitulo("Máscara Arquivo Entrada")
    String mascaraEntrada;

    @PlanilhaTitulo("Caminho Arquivo de Saída")
    String diretorioSaida;

    @PlanilhaTitulo("Máscara Arquivo Saída")
    String mascaraSaida;

    @PlanilhaTitulo("Caminho de logs")
    String diretorioLog;

    @PlanilhaTitulo("Máscara do Log Principal")
    String mascaraLog;

    @PlanilhaTitulo("Tratamento")
    String tratamento;

    @PlanilhaTitulo("Escalation")
    String escalation;

    @PlanilhaTitulo("Data de Atualização")
    String dataAtualizacao;

    @PlanilhaTitulo("Atualizado por:")
    String atualizadoPor;

//    public Map<String, String> mapearCamposValores() {
//        val camposValores = new HashMap<String, String>();
//        for(Field campo : this.getClass().getDeclaredFields()) {
//            try {
//                camposValores.put(
//                    campo.getAnnotation(PlanilhaTitulo.class).value(),
//                    String.valueOf(campo.get(this)).replace("\n", "\t")
//                );
//            }
//            catch(Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return camposValores;
//    }

    @Override
    public String pathShell() {
        if(!caminhoExec.endsWith("/")) caminhoExec = caminhoExec + "/";
        return caminhoExec + job;
    }

    @Override
    public List<String> pathLog() {
        return montarPath(diretorioLog, extrairMascara(mascaraLog));
    }

    @Override
    public List<String> pathSaida() {
        return montarPath(diretorioSaida, extrairMascara(mascaraSaida));
    }

    @Override
    public List<String> pathEntrada() {
        return montarPath(diretorioEntrada, extrairMascara(mascaraEntrada));
    }

    public List<String> getTabelasRefinadas() {
        if(valorVazio(tabelas)) return new ArrayList<>();
        return dividirValores(
            List.of("\n" ,  ";" , ","),
            tabelas.replace("RCVRY.", "")
        );
    }

    private static List<String> montarPath(String diretorio, String nome) {
        if(!diretorio.endsWith("/")) diretorio = diretorio + "/";
        
        if(valorVazio(diretorio) || valorVazio(nome))
            return new ArrayList<>();

        val finalDiretorio = diretorio;
        return dividirValores(nome)
            .stream()
            .map(log -> finalDiretorio + log)
            .collect(Collectors.toList());
    }
}
