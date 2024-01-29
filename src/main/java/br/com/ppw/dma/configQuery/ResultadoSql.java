package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultadoSql extends ComandoSql {

     boolean consultaPosJob = false;
     final List<List<Object>> resultadoPreJob = new ArrayList<>();
     final List<List<Object>> resultadoPosJob = new ArrayList<>();


     public ResultadoSql(@NonNull ComandoSql cmdSql) {
          addCampo(cmdSql.getCampos());
          setTabela(cmdSql.getTabela());
          getFiltros().addAll(cmdSql.getFiltros());
          setSql(cmdSql.getSql());
          setDescricao(cmdSql.getDescricao());
          setDinamico(cmdSql.isDinamico());
     }

     public ResultadoSql(@NonNull ConfigQuery configQuery) {
          super(configQuery);
     }

     @JsonIgnore
     public ResultadoSql fecharConsultaPreJob() {
          consultaPosJob = true;
          return this;
     }

     @JsonIgnore
     public String resumoPreJob() {
          return resumo(resultadoPreJob);
     }

     @JsonIgnore
     public String resumoPosJob() {
          return resumo(resultadoPosJob);
     }

     @JsonIgnore
     private String resumo(@NonNull List<List<Object>> conteudo) {
          final List<List<?>> tabela = new ArrayList<>();
          tabela.add(getCampos());
          tabela.addAll(conteudo);
          return FormatString.tabelaParaString(tabela);
     }

     @JsonIgnore
     public void addResultado(Map<String, Object> resultado) {
          //val tamanho = getCampos().size();
          //val novoRegistro = new ArrayList<>(Collections.nCopies(tamanho, null));
          //resultado.forEach((campo, valor) -> {
          //     val index = getCampos().indexOf(campo);
          //     if(index == -1) return;
          //     novoRegistro.set(index, valor);
          //});
          val novoRegistro = new ArrayList<>(resultado.values());
          if(!consultaPosJob) resultadoPreJob.add(novoRegistro);
          else resultadoPosJob.add(novoRegistro);
     }

     @Override
     public String toString() {
          return "ResultadoSql(" +
             "campos=" + getCampos() +
             ", tabela='" + getTabela() + '\'' +
             ", sql='" + getSql() + '\'' +
             ", filtros=" + getFiltros() +
             ", dinamico=" + isDinamico() +
             ", descricao='" + getDescricao() + '\'' +
             ", consultaPosJob=" + consultaPosJob +
             ", resultadoPreJob=" + resumoRegistros(resultadoPreJob) +
             ", resultadoPosJob=" + resumoRegistros(resultadoPosJob) +
             ')';
     }

     @JsonIgnore
     private String resumoRegistros(@NotEmpty List<List<Object>> registros) {
          return String.format("[registros=%d]", registros.size());
     }

}
