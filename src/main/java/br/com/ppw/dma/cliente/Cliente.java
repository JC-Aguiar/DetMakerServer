package br.com.ppw.dma.cliente;

import br.com.ppw.dma.ambiente.Ambiente;
import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.pipeline.Pipeline;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_CLIENTE")
@Table(name = "PPW_CLIENTE")
@SequenceGenerator(name = "SEQ_CLIENTE_ID", sequenceName = "RCVRY.SEQ_CLIENTE_ID", allocationSize = 1)
public class Cliente implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CLIENTE_ID")
    // Identificador numérico do cliente
    Long id;

    @Column(name = "NOME", length = 50, unique = true)
    // Nome do cliente
    String nome;

    @Lob
    @Column(name = "BANNER", columnDefinition = "BLOB")
    // Imagem do banner do cliente para ser usada no frontend
    byte[] banner;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "AMBIENTES")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "cliente")
    // IDs dos Ambientes relacionados a este Cliente
    List<Ambiente> ambientes = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "JOBS")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "cliente")
    // IDs dos Jobs relacionados a este Cliente
    List<Job> jobs = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "PIPELINES")
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "cliente")
    // IDs das Pipelines relacionadas a este Cliente
    List<Pipeline> pipelines = new ArrayList<>();


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
            ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Cliente cliente = (Cliente) o;
        return getId() != null && Objects.equals(getId(), cliente.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
