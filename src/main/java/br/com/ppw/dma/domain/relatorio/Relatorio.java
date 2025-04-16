package br.com.ppw.dma.domain.relatorio;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.master.MasterEntity;
import br.com.ppw.dma.domain.task.result.PipelineResult;
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
import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_RELATORIO")
@Table(name = "PPW_RELATORIO")
public class Relatorio implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_RELATORIO_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_RELATORIO_ID")
    Long id;

    @Column(name = "TICKET", length = 100, nullable = false, unique = true)
    @Comment("Identificador da solicitação de um acionamento (PPW_QUEUE)")
    String ticket;

    @Column(name = "PIPELINE_NOME", length = 200, nullable = false, updatable = false)
    @Comment("Nome da pipeline executada que gerou esse Relatório")
    String pipelineNome;

    @Column(name = "PIPELINE_DESCRICAO", length = 500, updatable = false)
    @Comment("Descrição da pipeline executada que gerou esse Relatório")
    String pipelineDescricao;

    @Column(name = "ID_PROJETO", length = 7) //, nullable = false, updatable = false)
    @Comment("Identificador invoice do projeto (padrão IN????)")
    String idProjeto;

    @Column(name = "NOME_PROJETO", length = 200) //, nullable = false, updatable = false)
    @Comment("Nome do projeto")
    String nomeProjeto;

    @Column(name = "NOME_ATIVIDADE", length = 300) //, updatable = false)
    @Comment("Nome da atividade")
    String nomeAtividade;

    @Column(name = "INCONFORMIDADES", length = 800, updatable = false)
    @Comment("Erros obtidos durante a geração das Evidências")
    String inconformidades;

    @Column(name = "TESTE_TIPO", length = 10, updatable = false)
    @Comment("Tipo de teste realizado pela Pipeline")
    TiposDeTeste testeTipo;

    @Column(name = "CLIENTE", length = 50, nullable = false, updatable = false)
    @Comment("Nome do Cliente")
    String cliente;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "SUCESSO", nullable = false, updatable = false)
    @Comment("Se a solicitação da execução (PPW_QUEUE) foi realizada sem erros fatais")
    Boolean sucesso;

    @Column(name = "ERRO_FATAL", length = 400, updatable = false)
    @Comment("Mensagem de erro durante a execução da tarefa (PPW_QUEUE) no Ambiente")
    String erroFatal;

    @Column(name = "USUARIO", length = 100, nullable = false, updatable = false)
    @Comment("Nome de quem solicitou a execução da Pipeline")
    String usuario;

    @Builder.Default
    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "EVIDENCIA", nullable = false, updatable = false)
    @OneToMany(fetch = LAZY, cascade = CascadeType.ALL, mappedBy = "relatorio")
    List<Evidencia> evidencias = new ArrayList<>();

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "AMBIENTE_ID", referencedColumnName = "ID", nullable = false, updatable = false)
    // ID do ambiente que executou esse relatório
    Ambiente ambiente;

    @Column(name = "DATA", columnDefinition = "DATE", nullable = false, updatable = false)
    @Comment("Data abreviada para consultas")
    LocalDate data;

    @Column(name = "DATA_COMPLETA", columnDefinition = "DATE", nullable = false, updatable = false)
    @Comment("Data completa para prover mais detalhes")
    OffsetDateTime dataCompleta;

    @Column(length = 500)
    @Comment("Comentários adicionados ao Relatório durante sua revisão")
    String consideracoes;


    public Relatorio(@NonNull PipelineResult pipelineResult) {
        var evidenciaErros = new StringBuilder();
        var evidenciasOk = new ArrayList<Evidencia>();
        for(var evResult : pipelineResult.getResultadoEvidencias()) {
            if(evResult.exception()) evidenciaErros.append(evResult.detalhes() + "\n");
            else evResult.evidencia().ifPresent(evidenciasOk::add);
        }
        this.cliente = pipelineResult.getClienteNome();
        this.ambiente = pipelineResult.getAmbiente();
        this.pipelineNome = pipelineResult.getPipelineNome();
        this.pipelineDescricao = pipelineResult.getPipelineDescricao();
        this.inconformidades = evidenciaErros.toString();
        this.ticket= pipelineResult.getTicket();
        this.usuario = pipelineResult.getUsuario();
        this.dataCompleta = OffsetDateTime.now(RELOGIO);
        this.data = dataCompleta.toLocalDate();
        this.testeTipo = TiposDeTeste.UNITARIO; //TODO: remover hardcoded
        this.sucesso = !pipelineResult.isErro();
        this.erroFatal = pipelineResult.getMensagemErro();
        setEvidencias(evidenciasOk);
//            .nomeAtividade(preparation.relatorio().getNomeAtividade())
//        relatorio.setIdProjeto(preparation.relatorio().getIdProjeto());
//        relatorio.setNomeProjeto(preparation.relatorio().getNomeProjeto());
//        relatorio.setTesteTipo(TiposDeTeste
//            .identificar(preparation.relatorio().getTesteTipo())
//            .orElse(null)
//        );
    }

    public void setIdProjeto(final String idProjeto) {
        if(this.idProjeto == null) this.idProjeto = idProjeto;
    }

    public void setNomeProjeto(final String nomeProjeto) {
        if(this.nomeProjeto == null) this.nomeProjeto = nomeProjeto;
    }

    public void setNomeAtividade(String nomeAtividade) {
        if(this.nomeAtividade == null) this.nomeAtividade = nomeAtividade;
    }

    public void setTesteTipo(final TiposDeTeste testeTipo) {
        if(testeTipo != null) this.testeTipo = testeTipo;
    }

    public boolean isEvidenciasRevisadas() {
        return evidencias.stream().allMatch(Evidencia::jaRevisada);
    }

    public boolean isCompleto() {
        return idProjeto != null && nomeProjeto != null && nomeAtividade != null;
    }

    public boolean podeGerarDet() {
        return isEvidenciasRevisadas() && isCompleto();
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
