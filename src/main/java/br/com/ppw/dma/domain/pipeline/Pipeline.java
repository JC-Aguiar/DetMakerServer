package br.com.ppw.dma.domain.pipeline;

import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.NumericBooleanConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;


@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_PIPELINE")
@Table(name = "PPW_PIPELINE", uniqueConstraints = @UniqueConstraint(columnNames = {"NOME", "CLIENTE_ID"} ))
public class Pipeline implements MasterEntity<Long> {

//    @Column(name = "ID", unique = true, nullable = false)
    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_PIPELINE_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_PIPELINE_ID")
    @Comment("Identificador numérico da pipeline")
    Long id;

    @Column(name = "NOME", length = 200)
    @Comment("Nome da pipeline")
    String nome;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CLIENTE_ID", referencedColumnName = "ID") )
    @Comment("ID do Cliente associado a esta Pipeline")
    Cliente cliente;

    @Column(name = "DESCRICAO", length = 500)
    @Comment("Nome da pipeline")
    String descricao;

    @JsonBackReference
    @Column(name = "JOBS")
    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "PPW_PIPELINE_JOB",
        joinColumns = @JoinColumn(name = "PIPELINE_ID", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "JOB_ID", referencedColumnName = "ID"))
    @Comment("IDs dos jobs relacionados a essa pipeline")
    List<Job> jobs = new ArrayList<>();

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "OCULTAR", nullable = false)
    @Comment("Se A Pipeline foi ou não removida da seleção de opções ativas")
    Boolean ocultar = false;


    public static Pipeline parseInfoDto(
        @NonNull PipelineInfoDTO dto,
        @NonNull List<Job> jobs,
        @NonNull Cliente cliente) {
        //------------------------------------------------------------------
//        val props = new PipelineProps(dto.getNome(), cliente);
        val pipeline = new Pipeline();
        pipeline.setNome(dto.getNome());
        pipeline.setCliente(cliente);
        pipeline.setDescricao(dto.getDescricao());
        pipeline.setJobs(jobs);
        return pipeline;
    }

    public boolean precisaAtualizarDescricao(String descricao) {
        if(this.descricao == null)
            return descricao == null;
        if(descricao == null)
            return this.descricao == null;
        return !this.descricao.trim().equals(descricao.trim());
    }

    public boolean precisaAtualizarJobs(@NonNull List<String> jobs) {
        val thisJobs = this.jobs.stream()
            .map(Job::getNome)
            .collect(Collectors.joining(", "));

        val otherJobs = String.join(", ", jobs);

        return !thisJobs.equals(otherJobs);
    }

    public int totalJobs() {
        return jobs.size();
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
        Pipeline evidencia = (Pipeline) o;
        return this.getId() != null && Objects.equals(this.getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        val jobsString = jobs.stream()
            .map(job -> "(" +job.getId()+ ") " +job.getNome())
            .collect(Collectors.joining(", "));
        return "Pipeline{" +
            "id=" + id +
            ", name='" + nome + '\'' +
            ", cliente=" + cliente +
            ", descricao='" + descricao + '\'' +
            ", jobs=[" + jobsString + "]" +
            '}';
    }
}
