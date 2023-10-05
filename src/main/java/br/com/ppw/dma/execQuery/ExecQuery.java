package br.com.ppw.dma.execQuery;

import br.com.ppw.dma.evidencia.Evidencia;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_EXEC_QUERY")
@Table(name = "PPW_EXEC_QUERY")
@SequenceGenerator(name = "SEQ_EXEC_QUERY_ID", sequenceName = "RCVRY.SEQ_EXEC_QUERY_ID", allocationSize = 1)
public class ExecQuery {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EXEC_QUERY_ID")
    // Identificador numérico dessa query pós-execução da evidência
    Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "EVIDENCIA_ID")
    // ID da evidência relacionada com esse query pós-execução
    Evidencia evidencia;

    @Column(name = "TABELA_NOME", length = 150)
    // Nome da tabela usada na query
    String tabelaNome;

    @Column(name = "QUERY", length = 500)
    // SQL usada na evidência desse query pós-execução
    String query;


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
        ExecQuery execFile = (ExecQuery) o;
        return getId() != null && Objects.equals(getId(), execFile.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
