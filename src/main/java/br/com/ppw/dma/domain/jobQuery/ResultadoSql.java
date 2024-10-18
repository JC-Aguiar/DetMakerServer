package br.com.ppw.dma.domain.jobQuery;

import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Getter
@ToString
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE)
public class ResultadoSql {

     @NonNull
     final String nome;

     @NonNull
     final String descricao;

     @NonNull
     final String query;

     final List<String> campos = new ArrayList<>();

     boolean successo = false;

     String mensagemErro = "";

     @ToString.Exclude
     final List<List<String>> resultado = new ArrayList<>();



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
     public String getResultadoAsString() {
          var tabela = new ArrayList<List<String>>();
          tabela.add(campos);
          tabela.addAll(resultado);
          return FormatString.tabelaParaString(tabela);
     }

     @JsonIgnore
     public List<List<String>> getResultado() {
          return List.copyOf(resultado);
     }

     public void addResultado(List<Map<String, Object>> extracao) {
          if(campos.isEmpty() && !extracao.isEmpty()) {
               campos.addAll(extracao.get(0).keySet());
          }
          resultado.clear();
          extracao.stream()
             .map(Map::values)
             .map(registro -> registro.stream()
                .map(String::valueOf)
                .toList())
           .forEach(resultado::add);
          successo = true;
     }

     public void setMensagemErro(@NonNull String mensagemErro) {
          resultado.clear();
          this.mensagemErro = mensagemErro;
          this.successo = false;
     }

     @JsonIgnore
     @ToString.Include(name = "resutlado")
     private String resumoRegistros() {
          return String.format("[registros=%d]", resultado.size());
     }

}
