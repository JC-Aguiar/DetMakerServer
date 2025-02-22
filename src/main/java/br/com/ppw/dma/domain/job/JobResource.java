package br.com.ppw.dma.domain.job;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_JOB_RESOURCE")
@Table(name = "PPW_JOB_RESOURCE", uniqueConstraints = @UniqueConstraint(columnNames = { "MASCARA", "JOB_ID" } ))
public class JobResource {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_JOB_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_JOB_ID")
    @Comment("Identificador numérico do recurso")
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "JOB_ID", referencedColumnName = "ID", nullable = false) )
    @Comment("ID do Job associado a este recurso")
    Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", length = 9, nullable = false)
    @Comment("O tipo desse recurso, no qual o Job consome ou produz ao ser executado")
    JobResourceType tipo;

    @Column(name = "DIRETORIO", length = 150, nullable = false)
    @Comment("Diretório do recurso")
    String diretorio;

    @Column(name = "MASCARA", length = 150, nullable = false)
    @Comment("Máscara do recurso")
    String mascara;

    @Column(name = "DESCRICAO", length = 350)
    @Comment("Descrição do recurso")
    String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACESSO", length = 9, nullable = false)
    @Comment("O modelo de acesso para obter este recurso")
    JobResourceAccessType acesso;

}
