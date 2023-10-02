package br.com.ppw.dma.evidencia;

import br.com.ppw.dma.job.ComandoSql;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtrcaoBanco {

     final ComandoSql comandoSql;
     final List<Map<String, Object>> resultadoQuery = new ArrayList<>();
     
     public ExtrcaoBanco addResultado(Map<String, Object> resultado) {
          this.resultadoQuery.add(resultado);
          return this;
     }
     
     public ExtrcaoBanco addResultado(List<Map<String, Object>> resultado) {
          this.resultadoQuery.addAll(resultado);
          return this;
     }
    
}
