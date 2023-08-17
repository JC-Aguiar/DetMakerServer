package br.com.ppw.dma.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class EvidenciaTabela {
    
     List<Map<String, Object>> resultado = new ArrayList<>();
     
     public EvidenciaTabela addResultado(Map<String, Object> resultado) {
          this.resultado.add(resultado);
          return this;
     }
     
     public EvidenciaTabela addResultado(List<Map<String, Object>> resultado) {
          this.resultado.addAll(resultado);
          return this;
     }
    
}
