package br.com.ppw.dma.domain.execQuery;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.jobQuery.ResultadoSql;
import br.com.ppw.dma.domain.master.MasterEntity;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.PRIVATE;


@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_EXEC_QUERY")
@Table(name = "PPW_EXEC_QUERY")
public class ExecQuery implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @SequenceGenerator( name = "SEQ_EXEC_QUERY_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_EXEC_QUERY_ID")
    Long id;

    @Column(name = "TICKET", length = 100, nullable = false)
    @Comment("Identificador da solicitação de um acionamento")
    String ticket;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "EVIDENCIA_ID")
    @Comment("ID da evidência relacionada com esse queries pós-execução")
    Evidencia evidencia;

    @Column(name = "JOB_NOME", length = 100, nullable = false)
    @Comment("Nome do job que gerou essa query")
    String jobNome;

    @Column(name = "QUERY_NOME", length = 150, nullable = false)
    @Comment("Nome da query usada na queries")
    String queryNome;

    @Column(name = "DESCRICAO", length = 300, nullable = false)
    @Comment("Descrição do que a query se propõem a fazer")
    String queryDescricao;

    @Column(name = "QUERY", length = 500, nullable = false)
    @Comment("SQL usada na evidência desse queries pós-execução")
    String query;

    @ToString.Exclude
    @Column(name = "RESULTADO_PRE_JOB", columnDefinition = "CLOB") //nullable = false ?
    @Comment("Conteúdo da table extraída")
    String resultadoPreJob = "";

    @ToString.Exclude
    @Column(name = "RESULTADO_POS_JOB", columnDefinition = "CLOB") //nullable = false ?
    @Comment("Conteúdo da table extraída")
    String resultadoPosJob = "";

    @Column(name = "INCONFORMIDADE", columnDefinition = "VARCHAR2(200)")
    @Comment("Mensagem do erro, caso tenha ocorrido algum")
    String inconformidade = "";


    public static ExecQuery montarEvidencia(
        @NonNull Evidencia evidencia,
        @NonNull ResultadoSql tabelaPre,
        @NonNull ResultadoSql tabelaPos) {
        //-----------------------------------
        var inconformidades = List.of(
            tabelaPre.getMensagemErro(),
            tabelaPos.getMensagemErro())
            .stream()
            .filter(Predicate.not(String::isEmpty))
            .collect(Collectors.joining("\n"));
        return ExecQuery.builder()
            .evidencia(evidencia)
            .ticket(evidencia.getTicket())
            .jobNome(evidencia.getJobNome())
            .queryNome(tabelaPos.getNome())
            .queryDescricao(tabelaPos.getDescricao())
            .query(tabelaPos.getQuery())
            .resultadoPreJob(tabelaPre.getResultadoAsString())
            .resultadoPosJob(tabelaPos.getResultadoAsString())
            .inconformidade(inconformidades)
            .build();
    }

    @ToString.Include(name = "resultadoPreJob")
    public String resumoResultadoPreJob() {
        return getResumoResultado(resultadoPreJob);
    }

    @ToString.Include(name = "resultadoPosJob")
    public String resumoResultadoPosJob() {
        return getResumoResultado(resultadoPosJob);
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
