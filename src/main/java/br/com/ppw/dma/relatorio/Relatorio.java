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

    @Column(name = "ID_PROJETO", length = 7, nullable = false, updatable = false)
    String idProjeto = "N/A";

    @Column(name = "NOME_PROJETO", length = 200, nullable = false, updatable = false)
    String nomeProjeto = "Anônimo";

    @Column(name = "NOME_ATIVIDADE", length = 300, updatable = false)
    String nomeAtividade;

    @Column(name = "PARAMETROS", length = 500, updatable = false)
    String parametros;

    @Column(name = "CONSIDERACOES", length = 500, updatable = false)
    String consideracoes;

    @Column(name = "TESTE_TIPO", length = 10, updatable = false)
    TiposDeTeste testeTipo;

    @Column(name = "SISTEMA", length = 25, nullable = false, updatable = false)
    String sistema;

    @Column(name = "AMBIENTE", length = 20, nullable = false, updatable = false)
    String ambiente;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "SUCESSO", nullable = false, updatable = false)
    Boolean sucesso;

    @JsonManagedReference
    @Column(name = "EVIDENCIA", nullable = false, updatable = false)
    @OneToMany(fetch = LAZY)
    @ToString.Exclude
    // IDs das evidências que compõem esse relatório
    List<Evidencia> evidencias = new ArrayList<>();

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "PIPELINE_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    // ID da pipeline que executou esse relatório
    Pipeline pipeline;

    @Column(name = "DATA_INICIO", columnDefinition = "DATE", nullable = false, updatable = false)
    OffsetDateTime dataInicio;

    @Column(name = "DATA_FIM", columnDefinition = "DATE", nullable = false, updatable = false)
    OffsetDateTime dataFim;


    public void setIdProjeto(final String idProjeto) {
        if(idProjeto != null) this.idProjeto = idProjeto;
    }

    public void setNomeProjeto(final String nomeProjeto) {
        if(nomeProjeto != null) this.nomeProjeto = nomeProjeto;
    }

    public void setTesteTipo(final TiposDeTeste testeTipo) {
        if(testeTipo != null) this.testeTipo = testeTipo;
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
        Relatorio evidencia = (Relatorio) o;
        return getId() != null && Objects.equals(getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
