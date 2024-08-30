package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_QUEUE")
@Table(name = "PPW_QUEUE") //, uniqueConstraints = @UniqueConstraint(columnNames = { "ID", "TICKET" } ))
@SequenceGenerator(name = "SEQ_QUEUE_ID", sequenceName = "RCVRY.SEQ_QUEUE_ID", allocationSize = 1)
public class Queue implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_QUEUE_ID")
    Long id;

    @Column(name = "TICKET", length = 100, nullable = false, unique = true)
    @Comment("Nome de identificação da solicitação")
    String ticket;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "AMBIENTE_ID", referencedColumnName = "ID")
    @Comment("Ambiente em que serão executadas as Tasks e Queryes")
    Ambiente ambiente;

    @Column(name = "PIPELINE", length = 200)
    @Comment("Nome da Pipeline")
    String pipeline;

    @Column(name = "USUARIO", length = 200)
    @Comment("Nome do usuário")
    String usuario;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "QUERY_ID")
    @OneToMany(fetch = LAZY, cascade = ALL)
    @Comment("Queries a serem executadas antes do início da Pipeline")
    List<QueueQuery> preQueries = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "QUERY_ID")
    @OneToMany(fetch = LAZY, cascade = ALL)
    @Comment("Queries a serem executadas após a conclusão da Pipeline")
    List<QueueQuery> posQueries = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "TASK_ID")
    @OneToMany(fetch = LAZY, cascade = ALL)
    @Comment("Tasks declaradas pela Pipeline a serem executadas")
    List<QueueTask> tasks = new ArrayList<>();

    @Column(name = "DATA_SOLICITACAO", columnDefinition = "DATE")
    @Comment("Data e hora em que a Pipeline foi solicitada")
    OffsetDateTime dataSolicitacao;

    @Column(name = "DATA_EXECUCAO", columnDefinition = "DATE")
    @Comment("Data e hora em que a Pipeline foi executada")
    OffsetDateTime dataExecucao;

    @Enumerated(STRING)
    @Column(name = "status", length = 12)
    @Comment("Status dessa solicitação")
    QueueStatus status;

}
