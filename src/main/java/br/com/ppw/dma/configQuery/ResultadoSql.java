package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultadoSql {

     final String nome;
     final String query;
     final List<String> campos = new ArrayList<>();
     boolean successo = false;
     String mensagemErro = "";

     @ToString.Exclude
     final List<List<Object>> resultado = new ArrayList<>();


     public ResultadoSql(@NonNull String query) {
          this("Anônima", query);
     }

     public ResultadoSql(@NonNull String nome, @NonNull String query) {
          this.nome = nome;
          this.query = query;
     }

     @JsonIgnore
     public ResultadoSql addCampo(@NotBlank String campo) {
          this.campos.add(campo);
          return this;
     }

     @JsonIgnore
     public ResultadoSql addCampo(@NotEmpty List<String> campos) {
          this.campos.addAll(campos);
          return this;
     }

     @JsonIgnore
     public List<String> getCampos() {
          return List.copyOf(campos);
     }

     @JsonIgnore
     @ToString.Include
     public String resumo() {
          final List<List<?>> tabela = new ArrayList<>();
          tabela.add(this.getCampos());
          tabela.addAll(this.resultado);
          return FormatString.tabelaParaString(tabela);
     }

     @JsonIgnore
     public List<List<Object>> getResultado() {
          return List.copyOf(resultado);
     }

     public void addResultado(List<Map<String, Object>> extracao) {
          if(getCampos().isEmpty() && !extracao.isEmpty()) {
               getCampos().addAll(extracao.get(0).keySet());
          }
          resultado.clear();
          extracao.parallelStream().forEach(
              registro -> resultado.add(List.of(registro.values()))
          );
          successo = true;
     }

     public void setMensagemErro(@NonNull String mensagemErro) {
          resultado.clear();
          this.mensagemErro = mensagemErro;
          this.successo = false;
     }

     @JsonIgnore
     @ToString.Include
     private String resumoRegistros() {
          return String.format("[registros=%d]", resultado.size());
     }

}
