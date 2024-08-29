package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

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

    @Column(name = "TICKET", length = 100, unique = true)
    String ticket;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "AMBIENTE_ID", referencedColumnName = "ID")
    Ambiente ambiente;

    @Column(name = "PIPELINE", length = 200)
    String pipeline;

    @Column(name = "USUARIO", length = 200)
    String usuario;

//    @ToString.Exclude
//    @JsonManagedReference
//    @Column(name = "BANCO_ID")
//    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
//    List<ExecQuery> banco = new ArrayList<>();
//
//
//    @ToString.Exclude
//    @JsonManagedReference
//    @Column(name = "LOG_ID")
//    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
//    List<ExecFile> logs = new ArrayList<>();

    @Column(name = "DATA_SOLICITACAO", columnDefinition = "DATE")
    OffsetDateTime dataSolicitacao;

    @Column(name = "DATA_EXECUCAO", columnDefinition = "DATE")
    OffsetDateTime dataExecucao;

    @Enumerated(STRING)
    @Column(name = "status", length = 12)
    QueueStatus status;


    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
