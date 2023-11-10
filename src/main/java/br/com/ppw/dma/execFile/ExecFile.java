package br.com.ppw.dma.execFile;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
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
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "EVIDENCIA_ID")
    // ID da evidência relacionada com esse arquivo pós-execução
    Evidencia evidencia;

    @Column(name = "JOB_NOME", length = 100, nullable = false)
    // Nome do job que gerou esse arquivo
    String jobNome;

    @Column(name = "TIPO", length = 10)
    // Informação para descrever se o arquivo é do tipo 'entrada', 'saída', 'log' ou 'terminal'
    String tipo;

    @Column(name = "ARQUIVO_NOME", length = 200, nullable = false)
    // Nome desse arquivo pós-execução
    String arquivoNome;

    @Column(name = "ARQUIVO", columnDefinition = "CLOB", nullable = false)
    // Conteúdo do arquivo pós-execução
    String arquivo;

    @Override
    public String toString() {
        return "ExecFile{" +
            "id=" + id +
            ", evidencia=" + evidencia +
            ", jobNome='" + jobNome + '\'' +
            ", tipo='" + tipo + '\'' +
            ", arquivoNome='" + arquivoNome + '\'' +
            ", arquivo=" + getResumoArquivo() +
            '}';
    }

    private String getResumoArquivo() {
        val tamanho = FormatString.contarSubstring(arquivo, "\n");
        val peso = arquivo.getBytes().length;
        return String.format("[registros=%d, peso=%dKbs]", tamanho, peso);
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
        ExecFile execFile = (ExecFile) o;
        return getId() != null && Objects.equals(getId(), execFile.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
