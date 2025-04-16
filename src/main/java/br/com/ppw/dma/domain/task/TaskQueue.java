//package br.com.ppw.dma.domain.task;
//
//import br.com.ppw.dma.domain.ambiente.Ambiente;
//import br.com.ppw.dma.domain.master.MasterEntity;
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import jakarta.annotation.Nullable;
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//import org.hibernate.annotations.Comment;
//import org.hibernate.proxy.HibernateProxy;
//
//import java.time.OffsetDateTime;
//import java.util.Objects;
//import java.util.UUID;
//
//import static jakarta.persistence.EnumType.STRING;
//import static jakarta.persistence.FetchType.EAGER;
//import static jakarta.persistence.GenerationType.SEQUENCE;
//import static lombok.AccessLevel.PRIVATE;
//
//@Getter
//@Setter
//@ToString
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@FieldDefaults(level = PRIVATE)
//@Entity(name = "PPW_QUEUE")
//@Table(name = "PPW_QUEUE")
//public class RemoteTask implements MasterEntity<Long> {
//
//    @Id @Column(name = "ID")
//    @SequenceGenerator(name = "SEQ_QUEUE_ID", allocationSize = 1)
//    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_QUEUE_ID")
//    Long id;
//
//    @Builder.Default
//    @Column(name = "TICKET", length = 100, nullable = false, unique = true)
//    @Comment("Identificador da solicitação de um acionamento")
//    String ticket = UUID.randomUUID().toString();
//
//    @ToString.Exclude
//    @JsonBackReference
//    @ManyToOne(fetch = EAGER)
//    @JoinColumn(name = "AMBIENTE_ID", referencedColumnName = "ID")
//    @Comment("Ambiente em que serão executadas os comando no Payload")
//    Ambiente ambiente;
//
//    @Column(name = "PIPELINE", length = 200) // nullable = false ?
//    @Comment("Nome da Pipeline")
//    String pipeline;
//
//    @Column(name = "USUARIO", length = 200, nullable = false)
//    @Comment("Nome do usuário")
//    String usuario;
//
//    @ToString.Exclude
//    @Column(name = "PAYLOAD", columnDefinition = "CLOB", nullable = false)
//    @Comment("Conteúdo Json da Pipeline solicitada")
//    String payload = "";
//
//    @Column(name = "DATA_SOLICITACAO", columnDefinition = "DATE", nullable = false)
//    @Comment("Data e hora em que a Pipeline foi solicitada")
//    OffsetDateTime dataSolicitacao;
//
//    @Column(name = "DATA_EXECUCAO", columnDefinition = "DATE")
//    @Comment("Data e hora em que a Pipeline foi executada")
//    OffsetDateTime dataExecucao;
//
//    @Enumerated(STRING)
//    @Column(name = "STATUS", length = 12, nullable = false)
//    @Comment("Status dessa solicitação")
//    TaskStatus status;
//
//    @Nullable
//    @Transient
//    TaskPayload payloadObj;
//
//
//    @Override
//    public final boolean equals(Object o) {
//        if(this == o) return true;
//        if(o == null) return false;
//        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
//            ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
//            o.getClass();
//        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
//            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
//            this.getClass();
//        if(thisEffectiveClass != oEffectiveClass) return false;
//        RemoteTask taskQueue = (RemoteTask) o;
//        return getId() != null && Objects.equals(getId(), taskQueue.getId());
//    }
//
//    @Override
//    public final int hashCode() {
//        return this instanceof HibernateProxy ?
//            ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() :
//            getClass().hashCode();
//    }
//}
