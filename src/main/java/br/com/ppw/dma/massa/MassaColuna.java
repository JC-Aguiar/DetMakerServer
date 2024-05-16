package br.com.ppw.dma.massa;

import br.com.ppware.api.MassaColunaDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_MASSA_COLUNA")
@Table(name = "PPW_MASSA_COLUNA")
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

    @Column(name = "TIPO", nullable = false, length = 20)
    String tipo;

    @Column(name = "TAMANHO_MAX")
    Integer tamanhoMax = 0; //TODO: zero aqui dá ruim?

    @Column(name = "OPCAO", length = 30)
    String opcao;


    public MassaColuna(@NonNull MassaColunaDTO dto) {
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.tamanhoMax = dto.getTamanhoMax();
        this.opcao = dto.getOpcao();
    }

    public MassaColunaDTO toDto() {
        return new MassaColunaDTO(nome, tipo, tamanhoMax, opcao);
    }
}
