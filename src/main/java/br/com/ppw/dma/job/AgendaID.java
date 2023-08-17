package br.com.ppw.dma.job;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
public class AgendaID implements Serializable {

    private static final long serialVersionUID = -7221011882314152345L;
    
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_AGENDA_ID")
    // Identificador numérico do artefato
    Long id;
    
    @Column(name = "PLANILHA", length = 40)
    // Nome da planilha em que o registro pertence
    String nomePlanilha;
    
    @Column(name = "NOME_ARQUIVO", length = 70)
    // Nome do arquivo em que o registro pertence
    String nomeArquivo;

}
