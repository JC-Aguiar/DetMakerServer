package br.com.ppw.dma.domain.massa;

import br.com.ppw.dma.domain.cliente.Cliente;
import br.com.ppw.dma.domain.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.type.NumericBooleanConverter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "PPW_MASSA_TABELA")
@Table(name = "PPW_MASSA_TABELA", uniqueConstraints = {@UniqueConstraint(columnNames = {"TABELA_NOME", "CLIENTE_ID"})})
@SequenceGenerator(name = "SEQ_MASSA_TABELA_ID", sequenceName = "RCVRY.SEQ_MASSA_TABELA_ID", allocationSize = 1)
public class MassaTabela implements MasterEntity {

    @Id @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MASSA_TABELA_ID")
    Long id;

    @Column(name = "TABELA_NOME", length = 35)
    String nome;

    @ToString.Exclude
    @JsonManagedReference
    @Column(name = "COLUNAS_ID")
    @OneToMany(fetch = LAZY, mappedBy = "tabelaId")
    List<MassaColuna> colunas = new ArrayList<>();

    @Convert(converter = NumericBooleanConverter.class)
    Boolean usaPessoa = false;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CLIENTE_ID", referencedColumnName = "ID") )
    Cliente cliente;


    public MassaTabela(@NonNull MassaTabelaDTO dto) {
        this.nome = dto.getNome();
        this.colunas = dto.getColunas()
            .parallelStream()
            .map(MassaColuna::new)
            .peek(col -> col.setTabelaId(this))
            .toList();
        this.usaPessoa = dto.getUsaPessoa();
    }

    public MassaTabelaDTO toDto() {
        return new MassaTabelaDTO(this);
//        var dto = new MassaTabelaDTO(this.name);
//        this.column
//            .parallelStream()
//            .map(MassaColuna::toDto)
//            .forEach(dto::addColuna);
//        dto.setUsaPessoa(this.usaPessoa);
//        return dto;
    }
}
