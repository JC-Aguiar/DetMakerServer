package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.jobQuery.QueueQueryType;
import br.com.ppw.dma.domain.master.MasterEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_QUEUE_QUERY")
@Table(name = "PPW_QUEUE_QUERY")
public class QueueQuery implements MasterEntity<Long> {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(
        name = "SEQ_QUEUE_QUERY_ID",
        sequenceName = "RCVRY.SEQ_QUEUE_QUERY_ID",
        allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_QUEUE_QUERY_ID")
    Long id;

    @Column(name = "SQL_NOME", length = 50, nullable = false)
    @Comment("Nome desta query")
    String nome = "Anônimo";

    @Column(name = "SQL", length = 900, nullable = false)
    @Comment("SQL que deverá ser executada.")
    String sql;

    @Column(name = "TICKET", length = 100, nullable = false)
    @Comment("Ticket da QUEUE associada a execução dessa query.")
    String ticket;

    @Enumerated(STRING)
    @Column(name = "ESCOPO", length = 150, nullable = false)
    @Comment("Tipo de proprietário dessa query.")
    QueueQueryType escopo;

}
