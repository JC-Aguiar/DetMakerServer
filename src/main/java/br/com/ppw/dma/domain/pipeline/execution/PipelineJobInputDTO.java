package br.com.ppw.dma.domain.pipeline.execution;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.*;
import java.util.stream.Collectors;

@Valid
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PipelineJobInputDTO {

   @NotNull @Min(0) Long id;
   @NotNull @Min(0) Integer ordem;
   String argumentos;
//   List<ComandoSql> queries = new ArrayList<>();
   Set<PipelineQueryInputDTO> queries = new HashSet<>();
   List<PipelineJobCargaDTO> cargas = new ArrayList<>();

   @JsonIgnore
   Map<String, String> variaveis = new HashMap<>();


   public PipelineJobInputDTO(@NonNull Evidencia evidencia) {
      id = evidencia.getJob().getId();
      ordem = evidencia.getOrdem();
      argumentos = evidencia.getArgumentos();
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

}
