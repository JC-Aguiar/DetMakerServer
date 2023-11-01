package br.com.ppw.dma.relatorio;

import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.pipeline.Pipeline;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.NumericBooleanConverter;

import java.time.OffsetDateTime;
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
@Entity(name = "PPW_RELATORIO")
@Table(name = "PPW_RELATORIO")
@SequenceGenerator(name = "SEQ_RELATORIO_ID", sequenceName = "RCVRY.SEQ_RELATORIO_ID", allocationSize = 1)
public class Relatorio implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RELATORIO_ID")
    Long id;

    @Column(name = "NOME_ATIVIDADE", length = 300)
    String nomeAtividade;

    @Column(name = "NOME_PROJETO", length = 200)
    String nomeProjeto;

    @Column(name = "PARAMETROS", length = 500)
    String parametros;

    @Column(name = "CONFIG", length = 500)
    String configuracao;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "SUCESSO")
    Boolean sucesso;

    @JsonManagedReference
    @Column(name = "EVIDENCIA")
    @OneToMany(fetch = LAZY)
    @ToString.Exclude
    // IDs das evidências que compõem esse relatório
    List<Evidencia> evidencias = new ArrayList<>();

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PIPELINE_ID", referencedColumnName = "ID")
    // ID da pipeline que executou esse relatório
    Pipeline pipeline;

    @Column(name = "DATA_INICIO", columnDefinition = "DATE")
    OffsetDateTime dataInicio;

    @Column(name = "DATA_FIM", columnDefinition = "DATE")
    OffsetDateTime dataFim;


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
        Relatorio evidencia = (Relatorio) o;
        return getId() != null && Objects.equals(getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
