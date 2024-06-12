package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.job.Job;
import br.com.ppw.dma.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
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
    // Identificador numérico dessa query
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "JOB_ID")
    // ID do job relacionado com essa configuração de query
    Job job;

    @Column(name = "SQL_NOME", length = 50, nullable = false)
    // Nome dessa configuração de query
    String nome;

    @Column(name = "SQL", length = 900, nullable = false)
    // SQL usada na evidência desse queries pré e pós-execução
    // Exemplo: SELECT * FROM EVENTOS_WEB WHERE EVACCT IN (${contratos}) AND EVDTPROC=${ifxdate}
    String sql;

    @ToString.Exclude
//    @JsonManagedReference
    @Column(name = "VARIAVEIS")
    @OneToMany(fetch = LAZY)
    // Lista das variáveis que constam dentro da SQL
    List<ConfigQueryVar> variaveis = new ArrayList<>();

    @Column(name = "DESCRICAO", length = 500, nullable = false)
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
