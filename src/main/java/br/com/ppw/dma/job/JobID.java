package br.com.ppw.dma.job;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
public class JobID implements Serializable {

    private static final long serialVersionUID = -7221011882314152345L;
    
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JOB_ID")
    // Identificador numérico do artefato
    Long id;

    @Column(name = "NOME", length = 75)
    // 'Nome da shell, serviço ou artefato'")
    String nome;

}
