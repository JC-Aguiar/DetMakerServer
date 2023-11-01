package br.com.ppw.dma.util;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@Builder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultadoSql extends ComandoSql {

     final List<Map<String, Object>> tabelasPreJob = new ArrayList<>();
     final List<Map<String, Object>> tabelasPosJob = new ArrayList<>();

     public ResultadoSql(@NonNull ComandoSql cmdSql) {
          setCampos(cmdSql.getCampos());
          setTabela(cmdSql.getTabela());
          setFiltros(cmdSql.getFiltros());
     }

     public ResultadoSql addResultadoPreJob(Map<String, Object> resultado) {
          this.tabelasPreJob.add(resultado);
          return this;
     }

     public ResultadoSql addResultadoPreJob(List<Map<String, Object>> resultado) {
          this.tabelasPreJob.addAll(resultado);
          return this;
     }

     public ResultadoSql addResultadoPosJob(Map<String, Object> resultado) {
          this.tabelasPosJob.add(resultado);
          return this;
     }

     public ResultadoSql addResultadoPosJob(List<Map<String, Object>> resultado) {
          this.tabelasPosJob.addAll(resultado);
          return this;
     }
    
}
