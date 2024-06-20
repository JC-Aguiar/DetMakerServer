package br.com.ppw.dma.configQuery;

import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class ResultadoSql extends ComandoSql {

     final List<String> campos = new ArrayList<>();

     @ToString.Exclude
     final List<List<Object>> resultado = new ArrayList<>();

     boolean successo = false;

     String mensagemErro = "";


     public ResultadoSql(@NonNull ComandoSql comando) {
          new ModelMapper().map(comando, this);
          getFiltros().addAll(comando.getFiltros());
          getValores().putAll(comando.getValores());
//          setNome(comando.getNome());
//          setSql(comando.getSql());
//          setDescricao(comando.getDescricao());
//          setDinamico(comando.isDinamico());
     }

     @JsonIgnore
     public ComandoSql addCampo(@NotBlank String campo) {
          this.campos.add(campo);
          return this;
     }

     @JsonIgnore
     public ComandoSql addCampo(@NotEmpty List<String> campos) {
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
          extracao.forEach(
              registro -> resultado.add(List.of(registro.values()))
          );
          successo = true;
     }

     public void setMensagemErro(@NonNull String mensagemErro) {
          this.mensagemErro = mensagemErro;
          this.successo = false;
     }

     @JsonIgnore
     private String resumoRegistros(@NotEmpty List<List<Object>> registros) {
          return String.format("[registros=%d]", registros.size());
     }

}
