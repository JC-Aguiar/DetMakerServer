package br.com.ppw.dma.execFile;

import br.com.ppw.dma.evidencia.Evidencia;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.io.File;
import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_EXEC_FILE")
@Table(name = "PPW_EXEC_FILE")
@SequenceGenerator(name = "SEQ_EXEC_FILE_ID", sequenceName = "RCVRY.SEQ_EXEC_FILE_ID", allocationSize = 1)
public class ExecFile {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EXEC_FILE_ID")
    // Identificador numérico do arquivo pós-execução da evidência
    Long id;

    @ToString.Exclude
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "EVIDENCIA_ID")
    // ID da evidência relacionada com esse arquivo pós-execução
    Evidencia evidencia;

    @Column(name = "NOME", length = 200)
    // Nome desse arquivo pós-execução
    String nome;

    @Column(name = "ARQUIVO")
    // Conteúdo do arquivo pós-execução
    File arquivo;


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
        ExecFile execFile = (ExecFile) o;
        return getId() != null && Objects.equals(getId(), execFile.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
