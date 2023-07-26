package br.com.ppw.dma.agenda;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AgendaPOST {
    
    private List<AgendaDTO> agendas;
    
}
