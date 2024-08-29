package br.com.ppw.dma.domain.job;

import br.com.ppw.dma.domain.evidencia.Evidencia;
import br.com.ppw.dma.domain.execQuery.ExecQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Range;

import java.util.*;
import java.util.stream.Collectors;

@Valid
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobExecuteDTO {

   @NotNull @Range(min = 0) Long id;
   @NotNull @Range(min = 0, max = 999) Integer ordem;
   String argumentos;
//   List<ComandoSql> queries = new ArrayList<>();
   Set<String> queries = new HashSet<>();
   List<JobCarga> cargas = new ArrayList<>();

   @JsonIgnore
   Map<String, String> variaveis = new HashMap<>();


   public JobExecuteDTO(@NonNull Evidencia evidencia) {
      id = evidencia.getJob().getId();
      ordem = evidencia.getOrdem();
      argumentos = evidencia.getArgumentos();
      queries = evidencia.getBanco()
          .stream()
//          .map(ComandoSql::new)
          .map(ExecQuery::getQuery)
          .collect(Collectors.toSet());
      cargas = evidencia.getCargas()
          .stream()
          .map(JobCarga::new)
          .toList();
   }

   public JobExecuteDTO addQuery(@NonNull String...query) {
      return addQuery(Set.of(query));
   }

   public JobExecuteDTO addQuery(@NonNull Set<String> queries) {
      this.queries.addAll(queries);
      return this;
   }

}
