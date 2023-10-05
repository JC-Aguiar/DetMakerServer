package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.execFile.ExecFile;
import br.com.ppw.dma.execQuery.ExecQuery;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.NumericBooleanConverter;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@ToString
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
    @ManyToOne(fetch = LAZY)
    @JoinColumns({
        @JoinColumn(name = "JOB_ID", referencedColumnName = "ID")
        //@JoinColumn(name = "JOB_NOME", referencedColumnName = "NOME")
    })
    Job job;

    @ToString.Exclude
    @Column(name = "CARGAS")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    Set<ExecFile> cargas = new LinkedHashSet<>();

    @ToString.Exclude
    @Column(name = "BANCO_PRE_JOB")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    Set<ExecQuery> bancoPreJob = new LinkedHashSet<>();

    @ToString.Exclude
    @Column(name = "BANCO_POS_JOB")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    Set<ExecQuery> bancoPosJob = new LinkedHashSet<>();

    @ToString.Exclude
    @Column(name = "LOGS")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    Set<ExecFile> logs = new LinkedHashSet<>();

    @ToString.Exclude
    @Column(name = "SAIDAS")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    Set<ExecFile> saidas = new LinkedHashSet<>();

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "SUCESSO")
    Boolean sucesso = false;

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
