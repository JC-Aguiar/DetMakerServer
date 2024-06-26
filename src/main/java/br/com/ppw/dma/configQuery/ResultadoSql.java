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

//     boolean consultaPosJob = false;
//     final List<List<Object>> resultadoPreJob = new ArrayList<>();
     final List<List<Object>> resultado = new ArrayList<>();
     boolean successo = false;
     String mensagemErro = "";


     public ResultadoSql(@NonNull ComandoSql cmdSql) {
          addCampo(cmdSql.getCampos());
          setNome(cmdSql.getNome());
          getFiltros().addAll(cmdSql.getFiltros());
          setSql(cmdSql.getSql());
          setDescricao(cmdSql.getDescricao());
          setDinamico(cmdSql.isDinamico());
     }

     public ResultadoSql(@NonNull ConfigQuery configQuery) {
          super(configQuery);
     }

     @JsonIgnore
     public String resumo() {
          return resumo(resultado);
     }

     @JsonIgnore
     private String resumo(@NonNull List<List<Object>> conteudo) {
          final List<List<?>> tabela = new ArrayList<>();
          tabela.add(getCampos());
          tabela.addAll(conteudo);
          return FormatString.tabelaParaString(tabela);
     }

     @JsonIgnore
     public void addResultado(List<Map<String, Object>> extracao) {
          if(getCampos().isEmpty()) {
               getCampos().addAll(extracao.get(0).keySet());
          }
          resultado.clear();
          extracao.forEach(
              registro -> resultado.add(List.of(registro.values()))
          );
          successo = true;
     }

     public void setMensagemErro(@NonNull String mensagemErro) {
          this.mensagemErro = mensagemErro;
          this.successo = false;
     }

     @Override
     public String toString() {
          return "ResultadoSql(" +
              "campos=" + getCampos() +
              ", tabela='" + getNome() + '\'' +
              ", sql='" + getSql() + '\'' +
              ", filtros=" + getFiltros() +
              ", dinamico=" + isDinamico() +
              ", descricao='" + getDescricao() + '\'' +
              ", resultado=" + resumoRegistros(resultado) +
              ", successo=" + successo +
              ", mensagemErro='" + mensagemErro + '\'' +
             ')';
     }



     @JsonIgnore
     private String resumoRegistros(@NotEmpty List<List<Object>> registros) {
          return String.format("[registros=%d]", registros.size());
     }

}
