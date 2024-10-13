//package br.com.ppw.dma.domain.evidencia;
//
//import br.com.ppw.dma.domain.master.MasterEntity;
//import br.com.ppw.dma.domain.relatorio.TiposDeTeste;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import jakarta.persistence.*;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//import org.hibernate.annotations.Comment;
//import org.hibernate.type.NumericBooleanConverter;
//
//import java.time.OffsetDateTime;
//import java.util.Objects;
//
//import static jakarta.persistence.EnumType.STRING;
//import static jakarta.persistence.FetchType.LAZY;
//
//@Getter
//@Setter
//@ToString
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
//@Entity(name = "PPW_REVISAO")
//@Table(name = "PPW_REVISAO")
//@SequenceGenerator(name = "SEQ_REVISAO_ID", sequenceName = "RCVRY.SEQ_REVISAO_ID", allocationSize = 1)
//public class Revisao implements MasterEntity<Long> {
//
//    @Id @Column(name = "ID")
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_REVISAO_ID")
//    Long id;
//
//    @Column(name = "TICKET", length = 100, nullable = false, unique = true)
//    @Comment("Identificador da solicitação de um acionamento")
//    String ticket;
//
//    @Builder.Default
//    @Column(name = "ID_PROJETO", length = 7) //, nullable = false, updatable = false)
//    String idProjeto = "N/A";
//
//    @Builder.Default
//    @Column(name = "NOME_PROJETO", length = 200) //, nullable = false, updatable = false)
//    String nomeProjeto = "Não Informado";
//
//    @Column(name = "NOME_ATIVIDADE", length = 300) //, updatable = false)
//    String nomeAtividade;
//
//    @Column(name = "PARAMETROS", length = 500, updatable = false)
//    String parametros;
//
//    @Column(name = "CONSIDERACOES", length = 500, updatable = false)
//    String consideracoes;
//
//    @Column(name = "TESTE_TIPO", length = 10, updatable = false)
//    TiposDeTeste testeTipo;
//
//    @Column(name = "CLIENTE", length = 50, nullable = false, updatable = false)
//    String cliente;
//
//    @Convert(converter = NumericBooleanConverter.class)
//    @Column(name = "SUCESSO", nullable = false, updatable = false)
//    Boolean erro;
//
//    @Column(name = "USUARIO", length = 100, nullable = false, updatable = false)
//    String usuario;
//
//    @JsonManagedReference
//    @ToString.Exclude
//    @OneToOne(fetch = LAZY)
//    @JoinColumn(name = "EVIDENCIA_ID", nullable = false, updatable = false)
//    // IDs das evidências que compõem esse relatório
//    Evidencia evidencias;
//
//    @Column(name = "REVISOR", length = 100)
//    @Comment("Nome de quem fez a revisão da evidência")
//    String revisor;
//
//    @Column(name = "DATA_REVISAO", columnDefinition = "DATE")
//    @Comment("Data de quando a revisão da evidência foi feita")
//    OffsetDateTime dataRevisao;
//
//    @Column(name = "REQUISITOS", length = 500)
//    @Comment("Quais os requisitos de cenário que se deseja alcançar")
//    String requisitos;
//
//    @Column(name = "COMENTARIO", length = 280)
//    @Comment("Comentários adicionais explicando o comportamento do Job na evidência")
//    String comentario;
//
//    @Enumerated(STRING)
//    @Column(name = "STATUS", length = 10)
//    @Comment("Status da evidência, com base nos requisitos")
//    TipoEvidenciaStatus status;
//
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Revisao revisao = (Revisao) o;
//        return Objects.equals(getId(), revisao.getId())
//            && Objects.equals(getTicket(), revisao.getTicket())
//            && Objects.equals(getIdProjeto(), revisao.getIdProjeto())
//            && Objects.equals(getNomeProjeto(), revisao.getNomeProjeto())
//            && Objects.equals(getNomeAtividade(), revisao.getNomeAtividade())
//            && Objects.equals(getParametros(), revisao.getParametros())
//            && Objects.equals(getConsideracoes(), revisao.getConsideracoes())
//            && getTesteTipo() == revisao.getTesteTipo()
//            && Objects.equals(getCliente(), revisao.getCliente())
//            && Objects.equals(getSucesso(), revisao.getSucesso())
//            && Objects.equals(getUsuario(), revisao.getUsuario())
//            && Objects.equals(getEvidencias(), revisao.getEvidencias())
//            && Objects.equals(getRevisor(), revisao.getRevisor())
//            && Objects.equals(getDataRevisao(), revisao.getDataRevisao())
//            && Objects.equals(getRequisitos(), revisao.getRequisitos())
//            && Objects.equals(getComentario(), revisao.getComentario())
//            && getStatus() == revisao.getStatus();
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(
//            getId(),
//            getTicket(),
//            getIdProjeto(),
//            getNomeProjeto(),
//            getNomeAtividade(),
//            getParametros(),
//            getConsideracoes(),
//            getTesteTipo(),
//            getCliente(),
//            getSucesso(),
//            getUsuario(),
//            getEvidencias(),
//            getRevisor(),
//            getDataRevisao(),
//            getRequisitos(),
//            getComentario(),
//            getStatus());
//    }
//}
