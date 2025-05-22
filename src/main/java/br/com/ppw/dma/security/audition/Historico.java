package br.com.ppw.dma.security.audition;


import br.com.ppw.dma.domain.master.MasterEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;

import java.time.OffsetDateTime;
import java.util.Objects;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "PPW_HISTORICO")
@Table(name = "PPW_HISTORICO")
@SequenceGenerator(name = "SEQ_HISTORICO_ID", allocationSize = 1)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Historico implements MasterEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_HISTORICO_ID")
    @Comment("Identificador numérico do job")
    Long id;

    @Column(name = "ENDPOINT", nullable = false, length = 300)
    @Comment("Endpoint da requisição")
    String endpoint;

    @Column(name = "METODO", nullable = false, length = 10)
    @Comment("Método da requisição")
    String metodo;

    @Column(name = "IP", nullable = false, length = 50)
    @Comment("Ip de quem fez a requisição")
    String ip;

    @Nullable
    @Column(name = "DISPOSITIVO", length = 280)
    @Comment("Dispositivo de quem fez a requisição")
    String dispositivo;

    @Column(name = "USUARIO", nullable = false, length = 100)
    @Comment("Nome do usuário que fez a requisição")
    String usuario;

    @Builder.Default
    @Column(name = "DATA", nullable = false)
    @Comment("Data da requisição")
    OffsetDateTime data = OffsetDateTime.now(RELOGIO);


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer()
            .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
            .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Historico historico = (Historico) o;
        return getId() != null && Objects.equals(getId(), historico.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
            .getPersistentClass()
            .hashCode() : getClass().hashCode();
    }
}
