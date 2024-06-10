package br.com.ppw.dma.execQuery;

import br.com.ppw.dma.configQuery.ResultadoSql;
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
@Entity(name = "PPW_EXEC_QUERY")
@Table(name = "PPW_EXEC_QUERY")
@SequenceGenerator(name = "SEQ_EXEC_QUERY_ID", sequenceName = "RCVRY.SEQ_EXEC_QUERY_ID", allocationSize = 1)
public class ExecQuery {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_EXEC_QUERY_ID")
    // Identificador numérico dessa queries pós-execução da evidência
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "EVIDENCIA_ID")
    // ID da evidência relacionada com esse queries pós-execução
    Evidencia evidencia;

    @Column(name = "JOB_NOME", length = 100, nullable = false)
    // Nome do job que gerou essa query
    String jobNome;

    @Column(name = "QUERY_NOME", length = 150, nullable = false)
    // Nome da query usada na queries
    String queryNome;

    @Column(name = "QUERY", length = 500, nullable = false)
    // SQL usada na evidência desse queries pós-execução
    String query;

    @Column(name = "RESULTADO_PRE_JOB", columnDefinition = "CLOB") //nullable = false ?
    // Conteúdo da tabela extraída
    String resultadoPreJob = "";

    @Column(name = "RESULTADO_POS_JOB", columnDefinition = "CLOB") //nullable = false ?
    // Conteúdo da tabela extraída
    String resultadoPosJob = "";

    @Column(name = "INCONFORMIDADE", columnDefinition = "VARCHAR2(200)")
    String inconformidade;


    public static ExecQuery montarEvidencia(
        @NonNull Evidencia evidencia,
        @NonNull ResultadoSql tabelaPre,
        @NonNull ResultadoSql tabelaPos) {
        //-----------------------------------
        val separador = tabelaPre.getMensagemErro() == null
            || tabelaPre.getMensagemErro().isEmpty() ?
            "" : "\n";
        return ExecQuery.builder()
            .evidencia(evidencia)
            .jobNome(evidencia.getJob().getNome())
            .queryNome(tabelaPos.getNome())
            .query(tabelaPos.getSqlCompleta())
            .resultadoPreJob(tabelaPre.resumo())
            .resultadoPosJob(tabelaPos.resumo())
            .inconformidade(tabelaPre.getMensagemErro() + separador + tabelaPos.getMensagemErro())
            //TODO: ?informações da pipeline?
            .build();
    }

    @Override
    public String toString() {
        return "ExecQuery{" +
            "id=" + id +
            ", evidencia=" + evidencia +
            ", jobNome='" + jobNome + '\'' +
            ", tabelaNome='" + queryNome + '\'' +
            ", query='" + query + '\'' +
            ", resultadoPreJob=" + getResumoResultado(resultadoPreJob) +
            ", resultadoPosJob=" + getResumoResultado(resultadoPosJob) +
            ", inconformidade='" + inconformidade + '\'' +
            '}';
    }

    private String getResumoResultado(@NonNull String resultado) {
        val tamanho = FormatString.contarSubstring(resultadoPreJob, "\n");
        val peso = resultado.getBytes().length;
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
        ExecQuery execFile = (ExecQuery) o;
        return getId() != null && Objects.equals(getId(), execFile.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
