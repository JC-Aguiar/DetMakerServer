package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.pipeline.Pipeline;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

import static jakarta.persistence.FetchType.LAZY;


@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_CONFIG_QUERY")
@Table(name = "PPW_CONFIG_QUERY")
@SequenceGenerator(name = "SEQ_CONFIG_QUERY_ID", sequenceName = "RCVRY.SEQ_CONFIG_QUERY_ID", allocationSize = 1)
public class ConfigQuery implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CONFIG_QUERY_ID")
    // Identificador numérico dessa queries pós-execução da evidência
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PIPELINE_ID")
    // ID da pipeline relacionada com essa configuração de queries
    Pipeline pipeline;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "JOB_ID")
    // ID do job relacionado com essa configuração de queries
    Job job;

    @Column(name = "TABELA_NOME", length = 50)
    // Nome da tabela usada na queries
    String tabelaNome;

    @Column(name = "SQL", length = 900)
    // SQL usada na evidência desse queries pré e pós-execução
    // Exemplo: SELECT * FROM EVENTOS_WEB WHERE ${EVACCT} IN (${string[]}) AND ${EVDTPROC}=${date(DD-MM-YYYYY)}
    String sql;

    @Column(name = "DESCRICAO", length = 500)
    // Informações sobre como preencher a SQL
    String descricao;


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
        ConfigQuery execFile = (ConfigQuery) o;
        return getId() != null && Objects.equals(getId(), execFile.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
