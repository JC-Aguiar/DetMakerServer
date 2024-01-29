package br.com.ppw.dma.pipeline;

import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.master.MasterEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.FetchType.LAZY;


@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineProps {

    @Column(name = "NOME", length = 200)
    // Nome da pipeline
    String nome;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CLIENTE_ID", referencedColumnName = "ID") )
    // ID do Cliente associado a esta Pipeline
    Cliente cliente; //TODO: precisa ser não-nulo
}
