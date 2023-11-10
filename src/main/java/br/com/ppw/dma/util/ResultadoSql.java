package br.com.ppw.dma.util;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultadoSql extends ComandoSql {

     static final String SEPARADOR_COLUNA = "||";
     //static final String SEPARADOR_CAMPOS = "-";

     boolean consultaPosJob = false;
     final List<List<Object>> resultadoPreJob = new ArrayList<>();
     final List<List<Object>> resultadoPosJob = new ArrayList<>();


     public ResultadoSql(@NonNull ComandoSql cmdSql) {
          setCampos(cmdSql.getCampos());
          setTabela(cmdSql.getTabela());
          setFiltros(cmdSql.getFiltros());
     }

     public ResultadoSql fecharConsultaPreJob() {
          consultaPosJob = true;
          return this;
     }

     public String getResumoPreJob() {
          return getResumo(resultadoPreJob);
     }

     public String getResumoPosJob() {
          return getResumo(resultadoPosJob);
     }

     private String getResumo(@NonNull List<List<Object>> conteudo) {
          final List<List<?>> tabela = new ArrayList<>();
          tabela.add(getCampos());
          tabela.addAll(conteudo);
          return FormatString.tabelaParaString(tabela);
     }

     public void addResultado(Map<String, Object> resultado) {
          val tamanho = getCampos().size();
          val novoRegistro = new ArrayList<>(Collections.nCopies(tamanho, null));
          resultado.forEach((campo, valor) -> {
               val index = getCampos().indexOf(campo);
               if(index == -1) return;
               novoRegistro.set(index, valor);
          });
          if(!consultaPosJob) resultadoPreJob.add(novoRegistro);
          else resultadoPosJob.add(novoRegistro);
     }

     @Override
     public String toString() {
          return "ResultadoSql(" +
             "campos=" + getCampos() +
             ", tabela='" + getTabela() + '\'' +
             ", filtros='" + getFiltros() + '\'' +
             ", rownumLimit=" + getRownumLimit() +
             ", consultaPosJob=" + consultaPosJob +
             ", resultadoPreJob=" + getResumoRegistros(resultadoPreJob) +
             ", resultadoPosJob=" + getResumoRegistros(resultadoPosJob) +
             ')';
     }

     private String getResumoRegistros(@NotEmpty List<List<Object>> registros) {
          return String.format("[registros=%d]", registros.size());
     }

}
