package br.com.ppw.dma.domain.cliente;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.pipeline.Pipeline;
import br.com.ppw.dma.domain.master.MasterEntity;
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
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_CLIENTE")
@Table(name = "PPW_CLIENTE")
public class Cliente implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_CLIENTE_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_CLIENTE_ID")
    // Identificador numérico do cliente
    Long id;

    @Column(name = "NOME", length = 50, unique = true, nullable = false)
    // Nome do cliente
    String nome;

    @Lob
    @ToString.Exclude
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

//    @Column(name = "OAUTH_ID") //unique = true, nullable = false)
//    private String oauthId;
//
//    @Column(name = "OAUTH_SECRET") //nullable = false)
//    private String oauthSecret;
//
//    @Convert(converter = StringListConverter.class)
//    @Column(name = "OAUTH_METODOS") //nullable = false)
//    private List<String> oauthMetodosAutenticacao;
//
//    @Convert(converter = StringListConverter.class)
//    @Column(name = "OAUTH_CONCESSOES") //nullable = false)
//    private List<String> oauthConcessoes;


    public Cliente(@NonNull ClienteNovoDTO dto) {
        this.nome = dto.getNome();
        this.banner = dto.getBanner();
    }

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
