package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_QUEUE_TASK")
@Table(name = "PPW_QUEUE_TASK")
public class QueueTask implements MasterEntity<Long> {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(
        name = "SEQ_QUEUE_TASK_ID",
        sequenceName = "RCVRY.SEQ_QUEUE_TASK_ID",
        allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_QUEUE_TASK_ID")
    Long id;

    @Column(name = "TICKET", length = 100, nullable = false)
    @Comment("Ticket da QUEUE associada a essa task.")
    String ticket;

    @Column(name = "NOME", length = 100, nullable = false)
    @Comment("Nome da Task a ser executada.")
    String nome;

    @Column(name = "ORDEM", precision = 3, nullable = false)
    @Comment("Índice na ordem de execução da Task pela QUEUE proprietária.")
    Integer ordem;

    @Column(name = "CHAMADA", length = 200, nullable = false)
    @Comment("Operação a ser executada (podendo ser HTTP ou SSH).")
    String chamada;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "BANCO_ID")
    @OneToMany(fetch = LAZY, cascade = ALL)
    @Comment("Queries a serem executadas por esta Task.")
    List<QueueQuery> queries = new ArrayList<>();

    @Column(name = "DIR_CARGA")
    @Comment("Diretório onde a Task irá consumir cargas.")
    String diretorioCarga;

    //TODO: @ToString.Exclude? @JsonManagedReference?
    @Column(name = "CARGA")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PPW_QUEUE_CARGA", joinColumns = @JoinColumn(name = "ID"))
    @Comment("Cargas a serem consumidas pela Task.")
    Set<String> cargas;

    @Column(name = "DIR_SAIDA")
    @Comment("Diretório onde a Task irá distribuir arquivos de saída.")
    String diretorioSaida;

    //TODO: @ToString.Exclude? @JsonManagedReference?
    @Column(name = "CARGA")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PPW_QUEUE_SAIDAS", joinColumns = @JoinColumn(name = "ID"))
    @Comment("Arquivos de saída a serem gerados pela Task.")
    Set<String> saidas;

    @Column(name = "DIR_LOG")
    @Comment("Diretório onde a Task irá gerar arquivos de log.")
    String diretorioLog;

    //TODO: @ToString.Exclude? @JsonManagedReference?
    @Column(name = "CARGA")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PPW_QUEUE_LOGS", joinColumns = @JoinColumn(name = "ID"))
    @Comment("Arquivos de logs a serem gerados pela Task.")
    Set<String> logs;


}
