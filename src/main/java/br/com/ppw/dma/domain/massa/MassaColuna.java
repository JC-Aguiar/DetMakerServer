package br.com.ppw.dma.domain.massa;

import br.com.ppware.api.FormatoMassa;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_MASSA_COLUNA")
@Table(name = "PPW_MASSA_COLUNA",
    uniqueConstraints = @UniqueConstraint(columnNames = {"TABELA_ID", "NOME"} ))
@SequenceGenerator(name = "SEQ_MASSA_COLUNA_ID", sequenceName = "RCVRY.SEQ_MASSA_COLUNA_ID", allocationSize = 1)
public class MassaColuna {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MASSA_COLUNA_ID")
    Long id;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "TABELA_ID", referencedColumnName = "ID") )
    MassaTabela tabelaId;

    @Column(name = "NOME", nullable = false, length = 25)
    String nome;

    @Enumerated(STRING)
    @Column(name = "FORMATO", nullable = false, length = 20)
    FormatoMassa formato;

//    @Column(name = "TAMANHO")//, nullable = false)
//    @Comment("Se a variável for de uma coluna do type texto, aqui consta o tamanho máximo de caracteres")
//    Integer tamanho;
//
//    @Column(name = "PRECISAO")//, nullable = false)
//    @Comment("Se a variável for de uma coluna do type numérica, aqui consta sua precisão")
//    Integer precisao;
//
//    @Column(name = "ESCALA")//, nullable = false)
//    @Comment("Se a variável for de uma coluna do type numérica, aqui consta sua escala")
//    Integer escala;

    @Column(name = "OPCAO", length = 50)
    String opcao;


    public MassaColuna(@NonNull MassaColunaDTO dto) {
        this.nome = dto.getNome();
//        this.tamanho = dto.getTamanho();
//        this.escala = dto.getEscala();
//        this.precisao = dto.getPrecisao();
        this.formato = dto.getFormato();
        this.opcao = dto.getOpcao();
    }

    public MassaColunaDTO toDto() {
        return new MassaColunaDTO(this);
    }

//    public MassaColunaDTO toDto(@NonNull DbColumnMetadata info) {
//        return new MassaColunaDTO(this, info);
//    }
}
