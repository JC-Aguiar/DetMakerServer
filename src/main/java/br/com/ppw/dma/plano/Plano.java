package br.com.ppw.dma.plano;

import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_PLANO")
@Table(name = "PPW_PLANO")
@SequenceGenerator(name = "SEQ_PLANO_ID", sequenceName = "RCVRY.SEQ_PLANO_ID", allocationSize = 1)
public class Plano implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PLANO_ID")
    // Identificador numérico do plano
    Long id;

    @Column(name = "NOME", unique = true)
    // Nome do plano
    String nome;

    @ToString.Exclude
    @Column(name = "JOBS")
    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "PPW_PLANO_JOB",
        joinColumns = @JoinColumn(name = "PLANO_ID", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "JOB_ID", referencedColumnName = "ID"))
    // IDs dos jobs relacionados a esse plano
    Set<Job> jobs = new LinkedHashSet<>();


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
        Plano evidencia = (Plano) o;
        return getId() != null && Objects.equals(getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
