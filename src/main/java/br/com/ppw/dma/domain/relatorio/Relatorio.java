package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.master.MasterEntity;
import br.com.ppw.dma.domain.queue.result.PipelineResult;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.NumericBooleanConverter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static br.com.ppw.dma.util.FormatDate.RELOGIO;
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

    @Column(name = "TICKET", length = 100, nullable = false, unique = true)
    @Comment("Identificador da solicitação de um acionamento")
    String ticket;

    @Builder.Default
    @Column(name = "ID_PROJETO", length = 7) //, nullable = false, updatable = false)
    String idProjeto = "N/A";

    @Builder.Default
    @Column(name = "NOME_PROJETO", length = 200) //, nullable = false, updatable = false)
    String nomeProjeto = "Não Informado";

    @Column(name = "NOME_ATIVIDADE", length = 300) //, updatable = false)
    String nomeAtividade;

    @Column(name = "PARAMETROS", length = 500, updatable = false)
    String parametros;

    @Column(name = "CONSIDERACOES", length = 500, updatable = false)
    String consideracoes;

    @Column(name = "TESTE_TIPO", length = 10, updatable = false)
    TiposDeTeste testeTipo;

    @Column(name = "CLIENTE", length = 50, nullable = false, updatable = false)
    String cliente;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "SUCESSO", nullable = false, updatable = false)
    Boolean sucesso;

    @Column(name = "USUARIO", length = 100, nullable = false, updatable = false)
    String usuario;

    @JsonManagedReference
    @Column(name = "EVIDENCIA", nullable = false, updatable = false)
    @OneToMany(fetch = LAZY)
    @ToString.Exclude
    // IDs das evidências que compõem esse relatório
    List<Evidencia> evidencias = new ArrayList<>();

//    @ToString.Exclude
//    @JsonBackReference
//    @ManyToOne(fetch = LAZY)
//    @JoinColumn(name = "PIPELINE_ID", referencedColumnName = "ID", nullable = false, updatable = false)
//    // ID da pipeline que executou esse relatório
//    Pipeline pipeline;

    @Column(name = "PIPELINE_NOME", length = 200)
    @Comment("Nome da pipeline executada que gerou esse relatório")
    String pipelineNome;

    @Column(name = "PIPELINE_DESCRICAO", length = 500)
    @Comment("Descrição da pipeline executada que gerou esse relatório")
    String pipelineDescricao;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "AMBIENTE_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    // ID do ambiente que executou esse relatório
    Ambiente ambiente;

    @Column(name = "DATA", columnDefinition = "DATE", nullable = false, updatable = false)
    LocalDate data;

    @Column(name = "DATA_COMPLETA", columnDefinition = "DATE", nullable = false, updatable = false)
    OffsetDateTime dataCompleta;


    public Relatorio(@NonNull Ambiente ambiente, @NonNull PipelineResult pipelineResult) {
        var consideracoes = new StringBuilder();
        var evidenciasOk = new ArrayList<Evidencia>();
        for(var ev : pipelineResult.getResultadoEvidencias()) {
            if(ev.exception()) consideracoes.append(ev.detalhes() + "\n");
            else ev.evidencia().ifPresent(evidenciasOk::add);
        }
//            .nomeAtividade(preparation.relatorio().getNomeAtividade())
            this.cliente = pipelineResult.getClienteNome();
            this.ambiente = ambiente;
            this.pipelineNome = pipelineResult.getPipelineNome();
            this.pipelineDescricao = pipelineResult.getPipelineDescricao();
            this.consideracoes = consideracoes.toString();
            this.ticket= pipelineResult.getTicket();
            this.usuario = pipelineResult.getUsuario();
//            .parametros(parametrosDosJobs)
            this.data = LocalDate.now(RELOGIO);
            this.dataCompleta = OffsetDateTime.now(RELOGIO);
            this.testeTipo = TiposDeTeste.UNITARIO; //TODO: remover hardcoded
            setEvidencias(evidenciasOk);
//        relatorio.setIdProjeto(preparation.relatorio().getIdProjeto());
//        relatorio.setNomeProjeto(preparation.relatorio().getNomeProjeto());
//        relatorio.setTesteTipo(TiposDeTeste
//            .identificar(preparation.relatorio().getTesteTipo())
//            .orElse(null)
//        );
    }

    public void setIdProjeto(final String idProjeto) {
        if(idProjeto != null) this.idProjeto = idProjeto;
    }

    public void setNomeProjeto(final String nomeProjeto) {
        if(nomeProjeto != null) this.nomeProjeto = nomeProjeto;
    }

    public void setTesteTipo(final TiposDeTeste testeTipo) {
        if(testeTipo != null) this.testeTipo = testeTipo;
    }

    public void setEvidencias(List<Evidencia> evidencias) {
        this.evidencias = evidencias;
        evidencias.forEach(ev -> ev.setRelatorio(this));
    }

    @Override
    public final boolean equals(Object o) {
        if(this == o) return true;
        if(o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy
            ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
            ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if(thisEffectiveClass != oEffectiveClass) return false;
        Relatorio evidencia = (Relatorio) o;
        return getId() != null && Objects.equals(getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
