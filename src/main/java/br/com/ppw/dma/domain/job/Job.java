package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.jobQuery.JobQuery;
import br.com.ppw.dma.domain.pipeline.Pipeline;
import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static br.com.ppw.dma.util.FormatString.refinarCelula;
import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_JOB")
@Table(name = "PPW_JOB", uniqueConstraints = @UniqueConstraint(columnNames = { "NOME", "CLIENTE_ID" } ))
@SequenceGenerator(name = "SEQ_JOB_ID", sequenceName = "RCVRY.SEQ_JOB_ID", allocationSize = 1)
public class Job implements MasterEntity<Long> {

    @Id
//    @Column(name = "ID", unique = true, nullable = false)
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JOB_ID")
    // Identificador numérico do job
    Long id;

//    @EmbeddedId
//    // Chave composta do Job
//    JobProps props;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CLIENTE_ID", referencedColumnName = "ID", nullable = false) )
    // ID do cliente associado a este job
    Cliente cliente;

    //@Column(name = "NOME", length = 100, nullable = false)
    @Column(name = "NOME", length = 100)
    // Nome da shell, serviço ou job
    String nome;

    @Column(name = "PLANO", length = 50)
    // Nome da planilha em que o registro pertence
    String plano;

    @Column(name = "EXEC_POS_JOB", length = 60)
    // Indica os IDs dos Jobs do qual se deve executar após conclusão
    String executarAposJob;

    @Column(name = "GRUPO_CONCOR", length = 25)
    // Grupo de concorrência
    String grupoConcorrencia;

    @Column(name = "FASE", length = 25)
    // Fase
    String fase;

    @Column(name = "DESCRICAO", length = 500)
    // Descrição do Job
    String descricao;

    @Column(name = "GRUPO_UDA", length = 5)
    // Grupo (UDAx)
    String grupoUda;

    @Column(name = "PROGRAMA", length = 150)
    // Programas envolvidos no Job
    String programa;

    @Column(name = "TABELAS", length = 300)
    // Tabelas atualizadas pelo Job
    String tabelas;

    @Column(name = "SERVIDOR", length = 30)
    // Servidor
    String servidor;

    @Column(name = "CAMINHO_EXEC", length = 100)
    // Caminho de execução do Job
    String caminhoExec;

    @Column(name = "PARAMETROS", length = 50)
    // Máscara dos parâmetros para executar o Job
    String parametros;

    @Column(name = "PARAM_DESCRICAO", length = 500)
    // Descrição dos parâmetros
    String descricaoParametros;

    @Column(name = "ENTRADA_DIR", length = 100)
    // Caminho de entrada para arquivo de carga
    String diretorioEntrada;

    @Column(name = "ENTRADA_MASK", length = 350)
    // Máscara do arquivo de entrada
    String mascaraEntrada;

    @Column(name = "SAIDA_DIR", length = 100)
    // Caminho de saída para arquivos pós-processamento
    String diretorioSaida;

    @Column(name = "SAIDA_MASK", length = 150)
    // Máscara do arquivo de saída
    String mascaraSaida;

    @Column(name = "LOG_DIR", length = 100)
    // Caminho para os arquivos de log
    String diretorioLog;

    @Column(name = "LOG_MASK", length = 300)
    // Máscara dos arquivos de log
    String mascaraLog;
    
    @Column(name = "TRATAMENTO", length = 25)
    // Tratamento
    String tratamento;
    
    @Column(name = "ESCALATION", length = 25)
    // Escalation
    String escalation;
    
    @Column(name = "DT_ATUALIZACAO")
    // Data da última atualização no Job
    OffsetDateTime dataAtualizacao;
    
    @Column(name = "ATUALIZADO_POR", length = 50)
    // Atualizado por
    String atualizadoPor;
    
    @Column(name = "ORIGEM", length = 50)
    // Origem de como esse Job foi criado ('planilha', 'api' ou 'banco')
    String origem;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "JOB_QUERY")
    @OneToMany(fetch = LAZY, mappedBy = "job")
    // IDs das configurações de queries relacionadas a este job
    List<JobQuery> queries = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "EVIDENCIAS")
    @OneToMany(fetch = LAZY, mappedBy = "job")
    // IDs das evidências relacionadas a este job
    List<Evidencia> evidencias = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "PIPELINES")
    @ManyToMany(fetch = LAZY, mappedBy = "jobs")
    // IDs das pipelines relacionadas a este job
    List<Pipeline> pipelines = new ArrayList<>();


    /**
     * Método para após conversão da classe {@link JobInfoDTO}, para garantir que os campos que
     * antes eram de listagem agora estão corretamente preenchidos como uma string. Os elementos deverão
     * estar separados por vírgula (',')
     * @return {@link Job} esse mesmo objeto
     */
    public Job refinarCampos() {
        executarAposJob = refinarCelula(executarAposJob);
        programa = refinarCelula(programa);
        tabelas = refinarCelula(tabelas);
        parametros = refinarCelula(parametros);
        descricaoParametros = refinarCelula(descricaoParametros);
        mascaraEntrada = refinarCelula(mascaraEntrada);
        mascaraSaida = refinarCelula(mascaraSaida);
        mascaraLog = refinarCelula(mascaraLog);
        return this;
    }

    public Evidencia getEvidenciaMaisRecente() {
        return evidencias.stream()
            .min(Comparator.comparing(Evidencia::getDataInicio))
            .orElseThrow();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Job job = (Job) o;
        return getId() != null && Objects.equals(getId(), job.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
