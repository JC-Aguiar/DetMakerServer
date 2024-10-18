package br.com.ppw.dma.domain.evidencia;

import br.com.ppw.dma.domain.execFile.ExecFile;
import br.com.ppw.dma.domain.execQuery.ExecQuery;
import br.com.ppw.dma.domain.master.MasterEntity;
import br.com.ppw.dma.domain.queue.result.JobResult;
import br.com.ppw.dma.domain.relatorio.Relatorio;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.Where;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.NumericBooleanConverter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_EVIDENCIA")
@Table(name = "PPW_EVIDENCIA")
public class Evidencia implements MasterEntity<Long> {

    @Id @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_EVIDENCIA_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_EVIDENCIA_ID")
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns(@JoinColumn(name = "RELATORIO_ID", referencedColumnName = "ID"))
    Relatorio relatorio;

    @Column(name = "TICKET", length = 100, nullable = false)
    @Comment("Identificador da solicitação de um acionamento")
    String ticket;

    @Column(name = "ORDEM", nullable = false)
    @Comment("Ordem em que o este job foi executado pela pipeline")
    Integer ordem;

    //TODO: Trocar relacionamento direto com entidade Job para apenas apontamento ao nome do Job
//    @ToString.Exclude
//    @JsonBackReference
//    @ManyToOne(fetch = LAZY)
//    @JoinColumns(@JoinColumn(name = "JOB_ID", referencedColumnName = "ID"))
//    Job job;

    @Column(name = "JOB_NOME", length = 100, nullable = false)
    @Comment("Nome do Job executado que gerou esta evidência")
    String jobNome;

    @Column(name = "JOB_DESCRICAO", length = 500)
    @Comment("Descrição explicando o que faz o Job")
    String jobDescricao;

//    @Column(name = "PARAMETROS", length = 200)
//    @Comment("Parâmetros usados na execução do Job")
//    String parametros;

    @Column(name = "COMANDO_EXEC", length = 300, nullable = false)
    @Comment("Comando de execução do Job")
    String comandoExec;

    @Column(name = "VERSAO", length = 65)
    String versao;

    @Column(name = "COMANDO_VERSAO", length = 200)
    @Comment("Comando para obter a versão do Job")
    String comandoVersao;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "QUERY_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    List<ExecQuery> queries = new ArrayList<>();

    @Column(name = "DIR_CARGA", length = 100)
    @Comment("Caso o Job consuma cargas, aqui é o apontamento para o diretório em que serão enviadas")
    String dirCarga;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "CARGAS_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "tipo = 'carga'")
    List<ExecFile> cargas = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "LOG_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "tipo = 'log'")
    List<ExecFile> logs = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "REMESSA_ID") //TODO: não está tendo mapeamento bidirecional
    @OneToMany(fetch = LAZY, cascade = ALL, mappedBy = "evidencia")
    @Where(clause = "tipo = 'remessa'")
    List<ExecFile> remessas = new ArrayList<>();

    @Column(name = "EXIT_CODE", columnDefinition = "NUMBER(3)")
    Integer exitCode;

    @Column(name = "MENSAGEM_ERRO", length = 500)
    String mensagemErro;

    @Builder.Default
    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "SUCESSO")
    Boolean sucesso = false;

    @Column(name = "DATA_INICIO", columnDefinition = "DATE", nullable = false)
    OffsetDateTime dataInicio;

    @Column(name = "DATA_FIM", columnDefinition = "DATE", nullable = false)
    OffsetDateTime dataFim;

    @Column(name = "REVISOR", length = 100)
    String revisor;

    @Column(name = "DATA_REVISAO", columnDefinition = "DATE")
    OffsetDateTime dataRevisao;

    @Column(name = "REQUISITOS", length = 500)
    String requisitos;

    @Column(name = "COMENTARIO", length = 280)
    String comentario;

    //TODO: mudar para algo mais claro, como 'classificação' (ajuste no banco, back e front)
    @Enumerated(STRING)
    @Column(name = "STATUS", length = 10)
    TipoEvidenciaStatus status;

    @Enumerated(STRING)
    @Column(name = "ESCOPO", length = 12, nullable = false)
    @Comment("Tipo de execução evidenciada.")
    EvidenciaEscopo escopo;


    public Evidencia(@NonNull JobResult jobResult) {
        this.ticket = jobResult.getTicket();
        this.ordem = jobResult.getOrdem();
        this.jobNome = jobResult.getNome();
        this.jobDescricao = jobResult.getDescricao();
        this.comandoExec = jobResult.getComandoExec();
        this.versao = jobResult.getVersao();
        this.comandoVersao = jobResult.getComandoVersao();
        this.dirCarga = jobResult.getDirCargaEnvio();
        this.exitCode = jobResult.getExitCode();
        this.mensagemErro = jobResult.getErroFatal();
        this.sucesso = jobResult.isSucesso();
        this.dataInicio = jobResult.getDataInicio();
        this.dataFim = jobResult.getDataFim();
        this.escopo = EvidenciaEscopo.PIPELINE_JOB; //TODO: atualizar
    }

    public final boolean jaRevisada() {
        return revisor != null && !revisor.isEmpty() && dataRevisao != null && status != null;
    }

    public void setRelatorio(@NonNull Relatorio relatorio) {
        this.relatorio = relatorio;
        this.relatorio.getEvidencias().add(this);
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
        Evidencia evidencia = (Evidencia) o;
        return getId() != null && Objects.equals(getId(), evidencia.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
