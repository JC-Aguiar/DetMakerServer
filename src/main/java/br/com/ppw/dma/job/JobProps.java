package br.com.ppw.dma.job;

import br.com.ppw.dma.cliente.Cliente;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static jakarta.persistence.FetchType.LAZY;


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SequenceGenerator(name = "SEQ_JOB_ID", sequenceName = "RCVRY.SEQ_JOB_ID", allocationSize = 1)
public class JobProps {

    @Column(name = "NOME", length = 100, nullable = false)
    // Nome da shell, serviço ou job
    String nome;

    @ToString.Exclude
    @JsonBackReference
    @ManyToOne(fetch = LAZY)
    @JoinColumns( @JoinColumn(name = "CLIENTE_ID", referencedColumnName = "ID", nullable = false) )
    // ID do cliente associado a este job
    Cliente cliente;

}
