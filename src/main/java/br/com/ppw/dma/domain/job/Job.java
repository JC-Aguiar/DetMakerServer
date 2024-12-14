package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.jobQuery.JobQuery;
import br.com.ppw.dma.domain.master.MasterEntity;
import br.com.ppw.dma.domain.pipeline.Pipeline;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static br.com.ppw.dma.util.FormatString.refinarCelula;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_JOB")
@Table(name = "PPW_JOB", uniqueConstraints = @UniqueConstraint(columnNames = { "NOME", "CLIENTE_ID" } ))
public class Job implements MasterEntity<Long> {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_JOB_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_JOB_ID")
    @Comment("Identificador numérico do job")
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CLIENTE_ID", referencedColumnName = "ID", nullable = false) )
    @Comment("ID do cliente associado a este job")
    Cliente cliente;

    //@Column(name = "NOME", length = 100, nullable = false)
    @Column(name = "NOME", length = 100)
    @Comment("Nome do Job")
    String nome;

    @Column(name = "PLANO", length = 50)
    @Comment("Nome da planilha em que o registro pertence")
    String plano;

    @Column(name = "EXEC_POS_JOB", length = 60)
    @Comment("Indica os IDs dos Jobs do qual se deve executar após conclusão")
    String executarAposJob;

    @Column(name = "GRUPO_CONCOR", length = 25)
    @Comment("Grupo de concorrência (?)")
    String grupoConcorrencia;

    @Column(name = "FASE", length = 25)
    @Comment("Fase (?)")
    String fase;

    @Column(name = "DESCRICAO", length = 500)
    @Comment("Descrição do Job")
    String descricao;

    @Column(name = "GRUPO_UDA", length = 5)
    @Comment("Grupo (UDAx)")
    String grupoUda;

    @Column(name = "PROGRAMA", length = 150)
    @Comment("Programas envolvidos no Job")
    String programa;

    @Column(name = "TABELAS", length = 300)
    @Comment("Tabelas atualizadas pelo Job")
    String tabelas;

    @Column(name = "SERVIDOR", length = 30)
    @Comment("Servidor")
    String servidor;

    @Column(name = "CAMINHO_EXEC", length = 100)
    @Comment("Caminho de execução do Job")
    String caminhoExec;

    @Column(name = "PARAMETROS", length = 50)
    @Comment("Nome dos parâmetros para executar o Job")
    String parametros;

    @Column(name = "PARAM_DESCRICAO", length = 500)
    @Comment("Descrição de cada um dos parâmetros")
    String descricaoParametros;

    @Column(name = "ENTRADA_DIR", length = 100)
    @Comment("Caminho do diretório de cargas")
    String diretorioEntrada;

    @Column(name = "ENTRADA_MASK", length = 350)
    @Comment("Máscara das cargas consumidas pelo Job")
    String mascaraEntrada;

    @Column(name = "SAIDA_DIR", length = 100)
    @Comment("Caminho do diretório de remessas")
    String diretorioSaida;

    @Column(name = "SAIDA_MASK", length = 150)
    @Comment("Máscara das remessas geradas pelo Job")
    String mascaraSaida;

    @Column(name = "LOG_DIR", length = 100)
    @Comment("Caminho para o diretório dos logs")
    String diretorioLog;

    @Column(name = "LOG_MASK", length = 300)
    @Comment("Máscara dos arquivos de log gerados pelo Job")
    String mascaraLog;
    
    @Column(name = "TRATAMENTO", length = 25)
    @Comment("Tratamento (?)")
    String tratamento;
    
    @Column(name = "ESCALATION", length = 25)
    @Comment("Escalation (?)")
    String escalation;
    
    @Column(name = "DT_ATUALIZACAO")
    @Comment("Data da última atualização no Job")
    OffsetDateTime dataAtualizacao;
    
    @Column(name = "ATUALIZADO_POR", length = 50)
    @Comment("Autor que fez a última atualização")
    String atualizadoPor;
    
    @Column(name = "ORIGEM", length = 50)
    @Comment("Origem de como esse Job foi criado (ex: 'planilha', 'api' ou 'banco')")
    String origem;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "JOB_QUERY")
    @OneToMany(fetch = LAZY, mappedBy = "job")
    @Comment("ID das configurações de queries relacionadas ao Job")
    List<JobQuery> queries = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "PIPELINES")
    @ManyToMany(fetch = LAZY, mappedBy = "jobs") //TODO: cascade = {CascadeType.PERSIST, CascadeType.MERGE} ?
    @Comment("IDs das pipelines relacionadas ao Job")
    List<Pipeline> pipelines = new ArrayList<>();


    /**
     * Método para após conversão da classe {@link JobInfoDTO}, para garantir que os campos que
     * antes eram de listagem agora estão corretamente preenchidos como uma string. Os elementos deverão
     * estar separados por vírgula (',')
     * @return {@link Job} esse mesmo objeto
     */
    public Job refinarCampos() {
        executarAposJob = FormatString.refinarCelula(executarAposJob);
        programa = FormatString.refinarCelula(programa);
        tabelas = FormatString.refinarCelula(tabelas);
        parametros = FormatString.refinarCelula(parametros);
        descricaoParametros = FormatString.refinarCelula(descricaoParametros);

        mascaraEntrada = FormatString.refinarCelula(mascaraEntrada);
        mascaraEntrada = FormatString.dividirValores(mascaraEntrada)
            .stream()
            .map(FormatString::extrairMascara)
            .collect(Collectors.joining(", "));

        mascaraSaida = FormatString.refinarCelula(mascaraSaida);
        mascaraSaida = FormatString.dividirValores(mascaraSaida)
            .stream()
            .map(FormatString::extrairMascara)
            .collect(Collectors.joining(", "));

        mascaraLog = FormatString.refinarCelula(mascaraLog);
        mascaraLog = FormatString.dividirValores(mascaraLog)
            .stream()
            .map(FormatString::extrairMascara)
            .collect(Collectors.joining(", "));

        return this;
    }

//    public Evidencia getEvidenciaMaisRecente() {
//        return evidencias.stream()
//            .min(Comparator.comparing(Evidencia::getDataInicio))
//            .orElseThrow();
//    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Job job = (Job) o;
        return getId() != null && Objects.equals(getId(), job.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
