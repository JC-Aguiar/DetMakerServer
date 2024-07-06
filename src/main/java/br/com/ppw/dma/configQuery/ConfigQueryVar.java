package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.master.MasterEntity;
import br.com.ppw.dma.util.TipoColuna;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;
import org.hibernate.type.NumericBooleanConverter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_CONFIG_QUERY_VAR")
@Table(name = "PPW_CONFIG_QUERY_VAR",
    uniqueConstraints = @UniqueConstraint(columnNames = {"NOME", "COLUNA", "CONFIG_QUERY_ID"})
)
@SequenceGenerator(name = "SEQ_CONFIG_QUERY_ID", sequenceName = "RCVRY.SEQ_CONFIG_QUERY_ID", allocationSize = 1)
public class ConfigQueryVar implements MasterEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_CONFIG_QUERY_ID")
    @Comment("Identificador único dessa variável de configuração de query")
    Long id;

    @Column(name = "COLUNA", length = 50)//, nullable = false)
    @Comment("Nome de uma das colunas usadas nos filtros da QUERY_CONFIG.SQL")
    String coluna;

    @Column(name = "NOME", length = 25)//, nullable = false)
    @Comment("Nome de uma das variáveis definidas nos filtros da QUERY_CONFIG.SQL")
    String nome;

    @Enumerated(value = STRING)
    @Column(name = "TIPO", length = 10, nullable = false)
    @Comment("O tipo da variável serve para auxiliar ambos usuário e aplicação")
    TipoColuna tipo = TipoColuna.UNSET;

    @Column(name = "TAMANHO")
    @Comment("Se a variável for de uma coluna do tipo texto, aqui consta o tamanho máximo de caracteres")
    Integer tamanho;

    @Column(name = "PRECISAO")
    @Comment("Se a variável for de uma coluna do tipo numérica, aqui consta sua precisão")
    Integer precisao;

    @Column(name = "ESCALA")
    @Comment("Se a variável for de uma coluna do tipo numérica, aqui consta sua escala")
    Integer escala;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "ARRAY")
    @Comment("Identifica se a variável pode ou não receber múltiplos valores")
    Boolean array;

    @Column(name = "INDICE")
    @Comment("Informa a ordem sequencial em que essa variável aparece nos filtros da QUERY_CONFIG.SQL (da esquerda para a direita)")
    Integer index;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CONFIG_QUERY_ID", referencedColumnName = "ID") )
    @Comment("Identificador de qual ConfigQuery essa variável pertence")
    ConfigQuery query;


    public ConfigQueryVar(@NonNull FiltroSql filtro) {
        atualizar(filtro);
    }

    public void atualizar(@NonNull FiltroSql filtro) {
        this.nome = filtro.getVariavel();
        this.tipo = filtro.getTipo();
        this.coluna = filtro.getColuna();
        this.index = filtro.getIndex();
        this.array = filtro.getArray();
        if (filtro.getMetaDados() == null) return;
        this.tamanho = filtro.getMetaDados().length();
        this.precisao = filtro.getMetaDados().precision();
        this.escala = filtro.getMetaDados().scale();
    }
}
