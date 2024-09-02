package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.execQuery.ExecQuery;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.job.JobProcess;
import br.com.ppw.dma.domain.master.MasterEntity;
import br.com.ppw.dma.domain.relatorio.Relatorio;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Where;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_EVIDENCIA")
@Table(name = "PPW_EVIDENCIA")
public class Evidencia implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @SequenceGenerator(
        name = "SEQ_EVIDENCIA_ID",
        sequenceName = "RCVRY.SEQ_EVIDENCIA_ID",
        allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_EVIDENCIA_ID")
    Long id;

    @Column(name = "TICKET", length = 100, nullable = false)
    @Comment("Identificador da solicitação de um acionamento")
    String ticket;

    @Column(name = "ORDEM")
    Integer ordem;

    //TODO: Trocar relacionamento direto com entidade Job para apenas apontamento ao nome do Job
    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns(@JoinColumn(name = "JOB_ID", referencedColumnName = "ID"))
    Job job;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns(@JoinColumn(name = "RELATORIO_ID", referencedColumnName = "ID"))
    Relatorio relatorio;

    @Column(name = "ARGUMENTOS", length = 300)
    String argumentos;

//    @Column(name = "ANALISE", length = 3000)
//    String analise;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "QUERY_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    List<ExecQuery> queries = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "CARGAS_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "type = 'carga'")
    List<ExecFile> cargas = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "LOG_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "type = 'log'")
    List<ExecFile> logs = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "SAIDA_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "type = 'saída'")
    List<ExecFile> saidas = new ArrayList<>();

    @Column(name = "EXIT_CODE", columnDefinition = "NUMBER(3)")
    Integer exitCode;

    @Column(name = "SHA256", length = 65)
    String sha256;

    @Column(name = "ERRO_FATAL", length = 200)
    String erroFatal;

//    @Convert(converter = NumericBooleanConverter.class)
//    @Column(name = "SUCESSO")
//    Boolean sucesso = false;

    @Column(name = "DATA_INICIO", columnDefinition = "DATE", nullable = false)
    OffsetDateTime dataInicio;

    @Column(name = "DATA_FIM", columnDefinition = "DATE", nullable = false)
    OffsetDateTime dataFim;

    @Column(name = "REVISOR", length = 100)
    String revisor;

    @Column(name = "DATA_REVISAO", columnDefinition = "DATE")
    OffsetDateTime dataRevisao;

    @Column(name = "REQUISITOS", length = 500)
    String requisitos;

    @Column(name = "COMENTARIO", length = 280)
    String comentario;

    @Enumerated(STRING)
    @Column(name = "RESULTADO", length = 10, nullable = false)
    TipoEvidenciaResultado resultado;

    @Enumerated(STRING)
    @Column(name = "status", length = 12, nullable = false)
    @Comment("Tipo de execução evidenciada.")
    EvidenciaEscopo status;


    public Evidencia(@NonNull JobProcess process) {
        this.job = process.getJob();
        this.ordem = process.getJobInputs().getOrdem();
        this.argumentos = process.getJobInputs().getArgumentos();
        this.dataInicio = process.getDataInicio();
        this.dataFim = process.getDataFim();
        this.sha256 = process.getSha256();
        this.exitCode = process.getExitCode();
        this.erroFatal = process.getErroFatal();
//        this.sucesso = process.isSucesso();
//        this.analise = process.getAnalise();
    }

    public final boolean jaRevisada() {
        return revisor != null && !revisor.isEmpty() && dataRevisao != null && resultado != null;
    }

    @Override
    public final boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if(thisEffectiveClass != oEffectiveClass) return false;
        Evidencia evidencia = (Evidencia) o;
        return getId() != null && Objects.equals(getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
