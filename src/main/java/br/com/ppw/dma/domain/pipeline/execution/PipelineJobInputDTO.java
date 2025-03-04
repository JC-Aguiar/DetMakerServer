package br.com.ppw.dma.domain.pipeline.execution;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.util.FormatString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineJobInputDTO {

   @NotNull @Min(0) Long id;
   @NotNull @Min(0) Integer ordem;
   List<PipelineJobParamDTO> argumentos;
   Set<PipelineQueryInputDTO> queries = new HashSet<>();
   List<PipelineJobCargaDTO> cargas = new ArrayList<>();

   @JsonIgnore
   Map<String, String> variaveis = new HashMap<>();


   public PipelineJobInputDTO(@NonNull Evidencia evidencia) {
//      id = evidencia.getJob().getId();
      ordem = evidencia.getOrdem();
//      argumentos = evidencia.getParametros();  TODO: corrigir (?!)
      queries = evidencia.getQueries()
          .stream()
          .map(PipelineQueryInputDTO::new)
          .collect(Collectors.toSet());
      cargas = evidencia.getCargas()
          .stream()
          .map(PipelineJobCargaDTO::new)
          .toList();
   }

   public PipelineJobInputDTO addQuery(@NonNull PipelineQueryInputDTO...query) {
      return addQuery(Set.of(query));
   }

   public PipelineJobInputDTO addQuery(@NonNull Set<PipelineQueryInputDTO> queries) {
      this.queries.addAll(queries);
      return this;
   }

   public void aplicarConfiguracoes() {
      //Aplicando as variáveis nos parâmetros
      argumentos.forEach(argInput -> {
         var novoArq = FormatString.substituirVariaveis(argInput.getValor(), variaveis);
          argInput.setValor(novoArq);
      });
      //Aplicando as variáveis nas queries
      queries.forEach(queryInput -> {
         var novaSql = FormatString.substituirVariaveis(queryInput.getSql(), variaveis);
         queryInput.setSql(novaSql);
      });
   }

}
