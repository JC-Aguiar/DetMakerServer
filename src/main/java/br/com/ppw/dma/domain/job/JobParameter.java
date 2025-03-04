package br.com.ppw.dma.domain.job;

import br.com.ppware.api.TipoColuna;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.type.NumericBooleanConverter;

import java.util.Optional;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = PRIVATE)
@Entity(name = "PPW_JOB_PARAMETER")
@Table(name = "PPW_JOB_PARAMETER", uniqueConstraints = @UniqueConstraint(columnNames = { "NOME", "JOB_ID" } ))
public class JobParameter {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "SEQ_JOB_ID", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_JOB_ID")
    @Comment("Identificador numérico do parâmetro")
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "JOB_ID", referencedColumnName = "ID", nullable = false) )
    @Comment("ID do Job associado a este parâmetro")
    Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", length = 9, nullable = false)
    @Comment("Tipo do valor do parâmetro")
    JobParameterType tipo;

    @Column(name = "FORMATO", length = 30)
    @Comment("Se o valor possui um padrão, preencher aqui apenas com o formato em si (datas no padrão Java)")
    String formato;

    @Column(name = "NOME", length = 30, nullable = false)
    @Comment("Nome do parâmetro")
    String nome;

    @Column(name = "DESCRICAO", length = 350)
    @Comment("Descrição do parâmetro")
    String descricao;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "OPCIONAL")
    @Comment("Se o parâmetro é opcional")
    Boolean opcional;


    public boolean getOpcional() {
        return Optional.ofNullable(opcional).orElse(false);
    }
}
