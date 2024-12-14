package br.com.ppw.dma.domain.jobQuery;

import br.com.ppw.dma.domain.job.Job;
import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.*;


@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_JOB_QUERY")
@Table(name = "PPW_JOB_QUERY")
public class JobQuery implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_JOB_QUERY_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_JOB_QUERY_ID")
    @Comment("Identificador numérico dessa query")
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "JOB_ID")
    @Comment("ID do job relacionado com essa configuração de query")
    Job job;

    @Column(name = "SQL_NOME", length = 50, nullable = false)
    @Comment("Nome dessa configuração de query")
    String nome;

    @Column(name = "DESCRICAO", length = 500, nullable = false)
    @Comment("Informações sobre como preencher a SQL")
    String descricao;

    @Column(name = "SQL", length = 900, nullable = false)
    @Comment("SQL usada na evidência desse queries pré e pós-execução. "
        + "Exemplo: SELECT * FROM EVENTOS_WEB WHERE EVACCT IN (${contratos})")
    String sql;


    public JobQuery(@NonNull QueryInfoDTO dto) {
        atualizar(dto);
    }

    public void atualizar(@NonNull QueryInfoDTO dto) {
        this.nome = dto.getNome();
        this.descricao = dto.getDescricao();
        this.sql = dto.getSql();
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
        JobQuery execFile = (JobQuery) o;
        return getId() != null && Objects.equals(getId(), execFile.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }

//    @JsonIgnore
//    public String buildSql() {
//        var mapaValores = variaveis.stream()
//            .collect(Collectors.toMap(
//                ConfigQueryVar::getNome,
//                ConfigQueryVar::gerarValorAleatorio
//            ));
//        return FormatString.substituirVariaveis(sql, mapaValores);
//    }
}
