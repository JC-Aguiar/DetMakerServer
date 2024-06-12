package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.master.MasterEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_CONFIG_QUERY_VAR")
@Table(name = "PPW_CONFIG_QUERY_VAR")
@SequenceGenerator(
    name = "SEQ_CONFIG_QUERY_VAR_ID",
    sequenceName = "RCVRY.SEQ_CONFIG_QUERY_VAR_ID",
    allocationSize = 1)
public class ConfigQueryVar implements MasterEntity<Long> {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_CONFIG_QUERY_ID")
    @Comment("Identificador único dessa variável de configuração de query")
    Long id;

    @Column(name = "NOME_VARIAVEL", length = 25)//, nullable = false)
    @Comment("Nome da variável que consta no SQL da QUERY_CONFIG")
    String nomeVariavel;

    @Column(name = "TIPO", length = 13)//, nullable = false)
    @Comment("O tipo da variável serve para auxiliar ambos usuário e aplicação")
    FiltroTipo tipo;

}
