package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.job.JobExecutePOJO;
import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.relatorio.Relatorio;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Where;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.NumericBooleanConverter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_EVIDENCIA")
@Table(name = "PPW_EVIDENCIA")
@SequenceGenerator(name = "SEQ_EVIDENCIA_ID", sequenceName = "RCVRY.SEQ_EVIDENCIA_ID", allocationSize = 1)
public class Evidencia implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EVIDENCIA_ID")
    Long id;

    @Column(name = "ORDEM")
    Integer ordem;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns({
        @JoinColumn(name = "JOB_ID", referencedColumnName = "ID")
        //@JoinColumn(name = "JOB_NOME", referencedColumnName = "NOME")
    })
    Job job;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns({
        @JoinColumn(name = "RELATORIO_ID", referencedColumnName = "ID")
        //@JoinColumn(name = "JOB_NOME", referencedColumnName = "NOME")
    })
    Relatorio relatorio;

    @Column(name = "ARGUMENTOS", length = 300)
    String argumentos;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "BANCO_ID")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    List<ExecQuery> banco = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "CARGAS_ID")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "tipo = 'carga'")
    List<ExecFile> cargas = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "LOG_ID")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "tipo = 'log'")
    List<ExecFile> logs = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "SAIDA_ID")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "tipo = 'saída'")
    List<ExecFile> saidas = new ArrayList<>();

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "SUCESSO")
    Boolean sucesso = false;

    @Column(name = "DATA_INICIO", columnDefinition = "DATE")
    OffsetDateTime dataInicio;

    @Column(name = "DATA_FIM", columnDefinition = "DATE")
    OffsetDateTime dataFim;

    @Column(name = "REVISOR", columnDefinition = "VARCHAR2(100)")
    String revisor;

    @Column(name = "DATA_REVISAO", columnDefinition = "DATE")
    OffsetDateTime dataRevisao;

    @Column(name = "REQUISITOS", columnDefinition = "VARCHAR2(500)")
    String requisitos;

    @Column(name = "COMENTARIO", columnDefinition = "VARCHAR2(280)")
    String comentario;

    @Column(name = "RESULTADO", columnDefinition = "VARCHAR2(10)")
    TipoEvidenciaResultado resultado;


    public static Evidencia jobPojoExecutado(@NonNull JobExecutePOJO jobPojo) {
        return Evidencia.builder()
            .job(jobPojo.getJob())
            .sucesso(jobPojo.isSucesso())
            .ordem(jobPojo.getOrdem())
            .argumentos(jobPojo.getArgumentos())
            .dataInicio(jobPojo.getDataInicio())
            .dataFim(jobPojo.getDataFim())
            .sucesso(jobPojo.isSucesso())
            .build();
    }

    public final boolean jaRevisada() {
        return revisor != null && !revisor.isEmpty() && dataRevisao != null && resultado != null;
    }

    @Override
    public final boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
            ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
            o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
            this.getClass();
        if(thisEffectiveClass != oEffectiveClass) return false;
        Evidencia evidencia = (Evidencia) o;
        return getId() != null && Objects.equals(getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
