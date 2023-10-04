package br.com.ppw.dma.job;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.pipeline.Pipeline;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "PPW_JOB")
@Table(name = "PPW_JOB")
@FieldDefaults(level = AccessLevel.PRIVATE)
@SequenceGenerator(name = "SEQ_JOB_ID", sequenceName = "RCVRY.SEQ_JOB_ID", allocationSize = 1)
public class Job implements MasterEntity<Long> {

    //@EmbeddedId
    //JobID id;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JOB_ID")
    // Identificador numérico do artefato
    Long id;

    @Column(name = "NOME", length = 100, unique = true, nullable = false)
    // 'Nome da shell, serviço ou artefato'")
    String nome;

    @Column(name = "PLANO", length = 50)
    // Nome da planilha em que o registro pertence
    String plano;
    
    @Column(name = "EXEC_POS_JOB", length = 60)
    // 'Indica os IDs dos Jobs do qual se deve executar após conclusão'")
    String executarAposJob;
    
    @Column(name = "GRUPO_CONCOR", length = 25)
    // 'Grupo de concorrência'")
    String grupoConcorrencia;
    
    @Column(name = "FASE", length = 25)
    // 'Fase'")
    String fase;
    
    @Column(name = "DESCRICAO", length = 500)
    // 'Descrição do Job'")
    String descricao;
    
    @Column(name = "GRUPO_UDA", length = 5)
    // 'Grupo (UDAx)'")
    String grupoUda;
    
    @Column(name = "PROGRAMA", length = 150)
    // 'Programas envolvidos no Job'")
    String programa;
    
    @Column(name = "TABELAS", length = 300)
    // 'Tabelas atualizadas pelo Job'")
    String tabelas;
    
    @Column(name = "SERVIDOR", length = 30)
    // 'Servidor'")
    String servidor;
    
    @Column(name = "CAMINHO_EXEC", length = 100)
    // 'Caminho de execução do Job'")
    String caminhoExec;

    @Column(name = "PARAMETROS", length = 50)
    // 'Máscara dos parâmetros para executar o Job'")
    String parametros;
    
    @Column(name = "PARAM_DESCRICAO", length = 500)
    // 'Descrição dos parâmetros'")
    String descricaoParametros;
    
    @Column(name = "ENTRADA_DIR", length = 100)
    // 'Caminho de entrada para arquivo de carga'")
    String diretorioEntrada;
    
    @Column(name = "ENTRADA_MASK", length = 350)
    // 'Máscara do arquivo de entrada'")
    String mascaraEntrada;
    
    @Column(name = "SAIDA_DIR", length = 100)
    // 'Caminho de saída para arquivos pós-processamento'")
    String diretorioSaida;
    
    @Column(name = "SAIDA_MASK", length = 150)
    // 'Máscara do arquivo de saída'")
    String mascaraSaida;
    
    @Column(name = "LOG_DIR", length = 100)
    // 'Caminho para os arquivos de log'")
    String diretorioLog;
    
    @Column(name = "LOG_MASK", length = 300)
    // 'Máscara dos arquivos de log'")
    String mascaraLog;
    
    @Column(name = "TRATAMENTO", length = 25)
    // 'Tratamento'")
    String tratamento;
    
    @Column(name = "ESCALATION", length = 25)
    // 'Escalation'")
    String escalation;
    
    @Column(name = "DT_ATUALIZACAO")
    // 'Data da última atualização no Job'")
    OffsetDateTime dataAtualizacao;
    
    @Column(name = "ATUALIZADO_POR", length = 50)
    // 'Atualizado por'")
    String atualizadoPor;
    
    @Column(name = "ULTIMO_AUTOR", length = 50)
    // 'Nome de quem salvou o registro no banco'")
    String autorAtualizacao;

    @ToString.Exclude
    @OneToMany(fetch = LAZY, mappedBy = "job")
    @Column(name = "EVIDENCIAS")
    Set<Evidencia> evidencias = new LinkedHashSet<>();

    @ToString.Exclude
    @ManyToMany(fetch = LAZY, mappedBy = "jobs")
    @Column(name = "PIPELINES")
    Set<Pipeline> pipelines = new LinkedHashSet<>();

}
